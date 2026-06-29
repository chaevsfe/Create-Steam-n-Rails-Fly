package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.custom_tracks.casing.CasingChecker;
import com.railwayteam.railways.content.custom_tracks.casing.SlabUseOnCurvePacket;
import com.railwayteam.railways.content.handcar.HandcarItem;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.AdventureUtils;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline.BezierPointSelection;
import com.zurrtum.create.client.content.trains.track.CurvedTrackInteraction;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = CurvedTrackInteraction.class, remap = false)
public abstract class MixinCurvedTrackInteraction {
    @Inject(
        method = "onClickInput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
            remap = true
        ),
        cancellable = true
    )
    private static void railways$encaseCurve(Minecraft minecraft, boolean keyPressed, CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = minecraft.player;
        if (AdventureUtils.isAdventure(player))
            return;

        BezierPointSelection result = TrackBlockOutline.result;
        TrackBlockEntity track = result.blockEntity();
        BezierTrackPointLocation location = result.loc();
        BlockPos curveTarget = location.curveTarget();
        Map<BlockPos, BezierConnection> connections = track.getConnections();
        BezierConnection connection = connections == null ? null : connections.get(curveTarget);
        if (connection != null && CRTrackMaterials.getType(connection.getMaterial()) == CRTrackMaterials.CRTrackType.MONORAIL)
            return;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof HandcarItem handcar && handcar.useOnCurve(result, held)) {
            player.swing(InteractionHand.MAIN_HAND);
            cir.setReturnValue(true);
            return;
        }

        if (!held.isEmpty()) {
            if (!(held.getItem() instanceof BlockItem block))
                return;
            if (!CasingChecker.isValid(block.getBlock()))
                return;
        }

        CRPackets.PACKETS.send(new SlabUseOnCurvePacket(track.getBlockPos(), curveTarget, BlockPos.containing(result.vec())));
        player.swing(InteractionHand.MAIN_HAND);
        cir.setReturnValue(true);
    }
}
