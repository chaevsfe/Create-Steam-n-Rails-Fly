package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRBlockPartials;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ConductorRenderer extends MobRenderer<ConductorEntity, ConductorRenderState, ConductorRenderModel> {
    public static final Identifier TEXTURE = Railways.asResource("textures/entity/conductor.png");

    public ConductorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ConductorRenderModel(ctx.bakeLayer(ConductorEntityModel.LAYER_LOCATION)), 0.2f);
        addLayer(new ConductorCapLayer(this, ctx.getModelSet()));
        addLayer(new ConductorRemoteLayer(this));
        addLayer(new ConductorFlagLayer(this));
        addLayer(new ConductorToolboxLayer(this));
    }

    @Override
    public ConductorRenderState createRenderState() {
        return new ConductorRenderState();
    }

    @Override
    public void extractRenderState(ConductorEntity conductor, ConductorRenderState state, float partialTick) {
        super.extractRenderState(conductor, state, partialTick);
        state.color = conductor.getColor();
        state.headStack = conductor.getItemBySlot(EquipmentSlot.HEAD);
        state.job = conductor.getJob();
        state.texture = textureFor(conductor, state.headStack);

        state.isRiding = conductor.isPassenger();
        state.isHoldingSchedules = conductor.isHoldingSchedulesClient();

        state.isCarryingToolbox = conductor.isCarryingToolbox();
        if (state.isCarryingToolbox) {
            var toolbox = conductor.getToolbox();
            ItemStack displayStack = conductor.getToolboxDisplayStack();
            state.toolboxColor = toolbox.getColor();
            state.toolboxBlockState = displayStack.getItem() instanceof BlockItem bi
                    ? bi.getBlock().defaultBlockState() : null;
            state.toolboxLidAngle = toolbox.lid.getValue(partialTick);
            state.toolboxDrawerOffset = toolbox.drawers.getValue(partialTick);
        }
    }

    @Override
    public Identifier getTextureLocation(ConductorRenderState state) {
        return state.texture;
    }

    private static Identifier textureFor(ConductorEntity conductor, ItemStack headItem) {
        String name = headItem.getHoverName().getString();
        if (name.startsWith("[sus]"))
            name = name.substring(5);

        if (!headItem.isEmpty()
            && headItem.getItem() instanceof ConductorCapItem
            && CRBlockPartials.CUSTOM_CONDUCTOR_SKINS.containsKey(name)) {
            return ensurePng(CRBlockPartials.CUSTOM_CONDUCTOR_SKINS.get(name));
        }

        if (conductor.getCustomName() != null) {
            Identifier texture = CRBlockPartials.CUSTOM_CONDUCTOR_SKINS_FOR_NAME.get(conductor.getCustomName().getString());
            if (texture != null)
                return ensurePng(texture);
        }

        return TEXTURE;
    }

    private static Identifier ensurePng(Identifier id) {
        if (id.getPath().endsWith(".png"))
            return id;
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + ".png");
    }
}
