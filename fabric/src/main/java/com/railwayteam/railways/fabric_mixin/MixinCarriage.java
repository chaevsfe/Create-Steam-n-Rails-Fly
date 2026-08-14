package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.MixinVariables;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Carriage.class, remap = false)
public abstract class MixinCarriage {
	@Shadow
	public Train train;

	@Shadow
	public abstract CarriageBogey leadingBogey();

	@Shadow
	public abstract CarriageBogey trailingBogey();

	@Shadow
	public abstract TravellingPoint getLeadingPoint();

	@Shadow
	public abstract TravellingPoint getTrailingPoint();

	@Inject(method = "isOnIncompatibleTrack", at = @At("HEAD"), cancellable = true)
	private void railways$isOnIncompatibleTrack(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(railways$isIncompatible(true) || railways$isIncompatible(false));
	}

	@WrapMethod(
		method = "travel(Lnet/minecraft/world/level/Level;Lcom/zurrtum/create/content/trains/graph/TrackGraph;DLcom/zurrtum/create/content/trains/entity/TravellingPoint;Lcom/zurrtum/create/content/trains/entity/TravellingPoint;I)D"
	)
	private double railways$markSwitchTravel(Level level, TrackGraph graph, double distance,
			TravellingPoint toFollowForward, TravellingPoint toFollowBackward, int type,
			Operation<Double> original) {
		boolean previouslyTravelling = MixinVariables.trackEdgeCarriageTravelling;
		if (train.navigation.isActive())
			MixinVariables.trackEdgeCarriageTravelling = true;
		try {
			return original.call(level, graph, distance, toFollowForward, toFollowBackward, type);
		} finally {
			MixinVariables.trackEdgeCarriageTravelling = previouslyTravelling;
		}
	}

	@Unique
	private boolean railways$isIncompatible(boolean leading) {
		CarriageBogey bogey = leading ? leadingBogey() : trailingBogey();
		TravellingPoint point = leading ? getLeadingPoint() : getTrailingPoint();
		if (point.edge == null)
			return false;

		Identifier trackType = CRTrackMaterials.getType(point.edge.getTrackMaterial());
		if (trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL))
			return false;
		AbstractBogeyBlock<?> bogeyBlock = bogey.type;
		return bogeyBlock.isOnIncompatibleTrack((Carriage) (Object) this, leading);
	}
}
