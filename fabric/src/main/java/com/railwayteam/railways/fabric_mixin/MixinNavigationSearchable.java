/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.switches.TrackSwitch;
import com.railwayteam.railways.content.switches.TrackSwitchBlock.SwitchState;
import com.railwayteam.railways.mixin_interfaces.IGenerallySearchableNavigation;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.EdgeData;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalEdgeGroup;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(value = Navigation.class, remap = false)
public abstract class MixinNavigationSearchable implements IGenerallySearchableNavigation {

    @Shadow public Train train;
    @Shadow public int ticksWaitingForSignal;

    @Override
    public void railways$searchGeneral(double maxDistance, boolean forward, PointTest pointTest) {
        railways$searchGeneral(maxDistance, -1, forward, pointTest);
    }

    @Override
    public void railways$searchGeneral(double maxDistance, double maxCost, boolean forward, PointTest pointTest) {
        TrackGraph graph = train.graph;
        if (graph == null) return;

        boolean skipValidCheck = false;
        Set<Identifier> validTypes = new HashSet<>();
        for (int i = 0; i < train.carriages.size(); i++) {
            Carriage carriage = train.carriages.get(i);
            // CarriageBogey.type is public; getStyle() is public
            var leadingType = carriage.leadingBogey().type;
            var trailingType = carriage.trailingBogey().type;
            if (leadingType.getTrackType(carriage.leadingBogey().getStyle()) == CRTrackType.UNIVERSAL) {
                if (i == 0) skipValidCheck = true;
            } else {
                if (i == 0 || skipValidCheck) {
                    validTypes.addAll(leadingType.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
                } else {
                    validTypes.retainAll(leadingType.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
                }
                skipValidCheck = false;
            }
            if (carriage.isOnTwoBogeys()) {
                if (trailingType.getTrackType(carriage.trailingBogey().getStyle()) != CRTrackType.UNIVERSAL) {
                    if (skipValidCheck) {
                        validTypes.addAll(trailingType.getValidPathfindingTypes(carriage.trailingBogey().getStyle()));
                    } else {
                        validTypes.retainAll(trailingType.getValidPathfindingTypes(carriage.trailingBogey().getStyle()));
                    }
                    skipValidCheck = false;
                }
            }
        }
        if (validTypes.isEmpty() && !skipValidCheck) return;

        Map<TrackEdge, Integer> penalties = new IdentityHashMap<>();
        boolean costRelevant = maxCost >= 0;
        if (costRelevant) {
            for (Train otherTrain : Create.RAILWAYS.trains.values()) {
                if (otherTrain.graph != graph) continue;
                if (otherTrain == train) continue;
                int navigationPenalty = otherTrain.getNavigationPenalty();
                otherTrain.getEndpointEdges().forEach(nodes -> {
                    if (nodes.either(Objects::isNull)) return;
                    for (boolean flip : Iterate.trueAndFalse) {
                        TrackEdge e = graph.getConnection(flip ? nodes.swap() : nodes);
                        if (e == null) continue;
                        int existing = penalties.getOrDefault(e, 0);
                        penalties.put(e, existing + navigationPenalty / 2);
                    }
                });
            }
        }

        TravellingPoint startingPoint = forward
                ? train.carriages.get(0).getLeadingPoint()
                : train.carriages.get(train.carriages.size() - 1).getTrailingPoint();

        Set<TrackEdge> visited = new HashSet<>();
        Map<TrackEdge, Pair<Boolean, Couple<TrackNode>>> reachedVia = new IdentityHashMap<>();
        PriorityQueue<FrontierEntry> frontier = new PriorityQueue<>();

        TrackNode initialNode1 = forward ? startingPoint.node1 : startingPoint.node2;
        TrackNode initialNode2 = forward ? startingPoint.node2 : startingPoint.node1;
        Map<TrackNode, TrackEdge> initialConnections = graph.getConnectionsFrom(initialNode1);
        if (initialConnections == null) return;
        TrackEdge initialEdge = initialConnections.get(initialNode2);
        if (initialEdge == null) return;

        double distanceToNode2 = forward
                ? initialEdge.getLength() - startingPoint.position
                : startingPoint.position;
        frontier.add(new FrontierEntry(distanceToNode2, 0, initialNode1, initialNode2, initialEdge));
        // Train.Penalties.RED_SIGNAL = 25 (package-private constant)
        int signalWeight = Mth.clamp(ticksWaitingForSignal * 2, 25, 200);

        Search: while (!frontier.isEmpty()) {
            FrontierEntry entry = frontier.poll();
            if (!visited.add(entry.edge)) continue;

            double distance = entry.distance;
            int penalty = entry.penalty;
            if (distance > maxDistance) continue;

            TrackEdge edge = entry.edge;
            TrackNode node1 = entry.node1;
            TrackNode node2 = entry.node2;

            if (costRelevant) penalty += penalties.getOrDefault(edge, 0);

            EdgeData signalData = edge.getEdgeData();
            if (signalData.hasPoints()) {
                for (TrackEdgePoint point : signalData.getPoints()) {
                    if (node1 == initialNode1 && point.getLocationOn(edge) < edge.getLength() - distanceToNode2)
                        continue;
                    if (costRelevant && distance + penalty > maxCost) continue Search;
                    if (!point.canNavigateVia(node2)) continue Search;
                    if (point instanceof SignalBoundary signal) {
                        if (signal.isForcedRed(node2)) {
                            penalty += 400; // Train.Penalties.REDSTONE_RED_SIGNAL
                            continue;
                        }
                        UUID group = signal.getGroup(node2);
                        if (group == null) continue;
                        SignalEdgeGroup signalEdgeGroup = Create.RAILWAYS.signalEdgeGroups.get(group);
                        if (signalEdgeGroup == null) continue;
                        if (signalEdgeGroup.isOccupiedUnless(signal)) {
                            penalty += signalWeight;
                            signalWeight /= 2;
                        }
                    }
                    if (point instanceof GlobalStation station) {
                        Train presentTrain = station.getPresentTrain();
                        boolean isOwnStation = presentTrain == train;
                        if (presentTrain != null && !isOwnStation)
                            penalty += 300; // Train.Penalties.STATION_WITH_TRAIN
                        if (station.canApproachFrom(node2) && pointTest.test(distance, distance + penalty, reachedVia,
                                Pair.of(Couple.create(node1, node2), edge), station))
                            return;
                        if (!isOwnStation)
                            penalty += 50; // Train.Penalties.STATION
                    }
                    if (pointTest.test(distance, distance + penalty, reachedVia,
                            Pair.of(Couple.create(node1, node2), edge), point))
                        return;
                }
            }

            if (costRelevant && distance + penalty > maxCost) continue;

            Map<TrackNode, TrackEdge> connectionsFrom = graph.getConnectionsFrom(node2);
            if (connectionsFrom == null) continue;
            List<Map.Entry<TrackNode, TrackEdge>> validTargets = new ArrayList<>();
            for (Map.Entry<TrackNode, TrackEdge> connection : connectionsFrom.entrySet()) {
                TrackNode newNode = connection.getKey();
                if (newNode == node1) continue;
                if (edge.canTravelTo(connection.getValue()))
                    validTargets.add(connection);
            }
            if (validTargets.isEmpty()) continue;

            for (Map.Entry<TrackNode, TrackEdge> target : validTargets) {
                if (!(skipValidCheck
                        || validTypes.contains(CRTrackMaterials.getType(target.getValue().getTrackMaterial()))
                        || CRTrackMaterials.getType(target.getValue().getTrackMaterial()) == CRTrackType.UNIVERSAL))
                    continue;
                TrackNode newNode = target.getKey();
                TrackEdge newEdge = target.getValue();
                double newDistance = newEdge.getLength() + distance;
                reachedVia.putIfAbsent(newEdge, Pair.of(validTargets.size() > 1, Couple.create(node1, node2)));
                frontier.add(new FrontierEntry(newDistance, penalty, node2, newNode, newEdge));
            }
        }
    }

    @Override
    public Pair<TrackSwitch, Pair<Boolean, Optional<SwitchState>>> railways$findNearestApproachableSwitch(boolean forward) {
        TrackGraph graph = train.graph;
        if (graph == null) return null;

        MutableObject<TrackSwitch> result = new MutableObject<>(null);
        MutableObject<Boolean> headOn = new MutableObject<>(false);
        MutableObject<SwitchState> targetState = new MutableObject<>(null);
        double acceleration = train.acceleration();
        double minDistance = 0;
        double maxDistance = Math.max(32, 1.5f * (train.speed * train.speed) / (2 * acceleration));

        railways$searchGeneral(maxDistance, forward, (distance, cost, reachedVia, currentEntry, trackPoint) -> {
            if (distance < minDistance) return false;
            TrackEdge edge = currentEntry.getSecond();
            double position = edge.getLength() - trackPoint.getLocationOn(edge);
            if (distance - position < minDistance) return false;
            if (trackPoint instanceof TrackSwitch sw) {
                TrackNode node = currentEntry.getFirst().getSecond();
                headOn.setValue(sw.isPrimary(node));
                result.setValue(sw);
                if (!headOn.getValue()) {
                    for (TrackEdge reachedEdge : reachedVia.keySet()) {
                        SwitchState state = sw.getTargetState(reachedEdge.node1.getLocation());
                        if (state == null)
                            state = sw.getTargetState(reachedEdge.node2.getLocation());
                        targetState.setValue(state);
                        if (state != null) break;
                    }
                }
                return true;
            }
            return false;
        });

        return Pair.of(result.getValue(), Pair.of(headOn.getValue(), Optional.ofNullable(targetState.getValue())));
    }
}
