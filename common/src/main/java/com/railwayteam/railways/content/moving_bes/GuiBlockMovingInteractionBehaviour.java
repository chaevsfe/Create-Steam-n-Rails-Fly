package com.railwayteam.railways.content.moving_bes;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class GuiBlockMovingInteractionBehaviour extends MovingInteractionBehaviour {
    @Override
    public boolean handlePlayerInteraction(
        Player player,
        InteractionHand activeHand,
        BlockPos localPos,
        AbstractContraptionEntity contraptionEntity
    ) {
        if (player.level().isClientSide()) {
            return true;
        }

        Contraption contraption = contraptionEntity.getContraption();
        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info == null) {
            return false;
        }

        info.state().useWithoutItem(
            new GuiBlockContraptionWorld(player.level(), contraption, localPos),
            player,
            new BlockHitResult(Vec3.atCenterOf(localPos), Direction.DOWN, localPos, false)
        );
        return true;
    }
}
