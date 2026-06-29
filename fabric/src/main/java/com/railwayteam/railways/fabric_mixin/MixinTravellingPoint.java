package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.mixin_interfaces.ISwitchDisabledEdge;
import com.railwayteam.railways.util.MixinVariables;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TravellingPoint.class, remap = false)
public class MixinTravellingPoint {
	@Shadow
	public TrackEdge edge;

	@Inject(
		method = "travel(Lcom/zurrtum/create/content/trains/graph/TrackGraph;DLcom/zurrtum/create/content/trains/entity/TravellingPoint$ITrackSelector;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$IEdgePointListener;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$ITurnListener;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$IPortalListener;)D",
		at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/graph/TrackGraph;getConnectionsFrom(Lcom/zurrtum/create/content/trains/graph/TrackNode;)Ljava/util/Map;", ordinal = 2)
	)
	private void railways$flipEdgeCheck(TrackGraph graph, double distance, TravellingPoint.ITrackSelector trackSelector,
										TravellingPoint.IEdgePointListener signalListener,
										TravellingPoint.ITurnListener turnListener,
										TravellingPoint.IPortalListener portalListener,
										CallbackInfoReturnable<Double> cir) {
		MixinVariables.trackEdgeTemporarilyFlipped = true;
	}

	@Inject(
		method = "travel(Lcom/zurrtum/create/content/trains/graph/TrackGraph;DLcom/zurrtum/create/content/trains/entity/TravellingPoint$ITrackSelector;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$IEdgePointListener;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$ITurnListener;Lcom/zurrtum/create/content/trains/entity/TravellingPoint$IPortalListener;)D",
		at = @At("RETURN")
	)
	private void railways$selectEdge(TrackGraph graph, double distance, TravellingPoint.ITrackSelector trackSelector,
									 TravellingPoint.IEdgePointListener signalListener,
									 TravellingPoint.ITurnListener turnListener,
									 TravellingPoint.IPortalListener portalListener,
									 CallbackInfoReturnable<Double> cir) {
		if (MixinVariables.trackEdgeCarriageTravelling && edge != null
			&& ISwitchDisabledEdge.isAutomatic(edge)
			&& ISwitchDisabledEdge.isDisabled(edge)) {
			ISwitchDisabledEdge.automaticallySelect(edge);
		}
	}
}
