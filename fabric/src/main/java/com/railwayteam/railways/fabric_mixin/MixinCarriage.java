package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Carriage.class, remap = false)
public abstract class MixinCarriage {
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

	@Unique
	private boolean railways$isIncompatible(boolean leading) {
		CarriageBogey bogey = leading ? leadingBogey() : trailingBogey();
		TravellingPoint point = leading ? getLeadingPoint() : getTrailingPoint();
		if (point.edge == null)
			return false;

		Identifier trackType = CRTrackMaterials.getType(point.edge.getTrackMaterial());
		AbstractBogeyBlock<?> bogeyBlock = bogey.type;
		Identifier bogeyTrackType = bogeyBlock.getTrackType(bogey.getStyle());

		if (trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL))
			return false;
		if (bogeyTrackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL))
			return false;
		return !trackType.equals(bogeyTrackType);
	}
}
