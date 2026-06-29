package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.mixin_interfaces.ISwitchDisabledEdge;
import com.railwayteam.railways.util.MixinVariables;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrackEdge.class, remap = false)
public class MixinTrackEdge {
	@Inject(method = "canTravelTo", at = @At("HEAD"), cancellable = true)
	private void railways$travelThroughSwitches(TrackEdge other, CallbackInfoReturnable<Boolean> cir) {
		if (MixinVariables.signalPropagatorCallDepth > 0 || MixinVariables.temporarilySkipSwitches)
			return;

		TrackEdge relevantEdge = MixinVariables.trackEdgeTemporarilyFlipped ? (TrackEdge) (Object) this : other;
		MixinVariables.trackEdgeTemporarilyFlipped = false;

		if (MixinVariables.navigationCallDepth > 0 && ISwitchDisabledEdge.isAutomatic(relevantEdge))
			return;
		if (MixinVariables.trackEdgeCarriageTravelling
			&& ISwitchDisabledEdge.isAutomatic(relevantEdge)
			&& ISwitchDisabledEdge.isDisabled(relevantEdge))
			return;
		if (ISwitchDisabledEdge.isDisabled(relevantEdge))
			cir.setReturnValue(false);
	}
}
