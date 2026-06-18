package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.model.obj.ObjMaterialLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Create Fly's OBJ loader only skips faces whose material is null, not faces whose material has no
 * diffuse texture map (e.g. the "none" material used for intentionally untextured faces in Blender
 * exports). Baking such a face passes a null texture name into the sprite resolver and throws an
 * NPE, which aborts the whole model reload (black screen). This guard mirrors the existing
 * {@code mat == null} check so those faces are skipped instead of crashing, letting the Railways
 * OBJ models (boilers, bogeys, buffers, gauge bases) render.
 */
@Mixin(targets = "com.zurrtum.create.client.model.obj.ObjGeometry$ModelMesh", remap = false)
public abstract class MixinObjGeometryModelMesh {
    @Shadow
    public ObjMaterialLibrary.Material mat;

    @Inject(method = "addQuads", at = @At("HEAD"), cancellable = true, remap = false)
    private void railways$skipUntexturedFaces(CallbackInfo ci) {
        if (mat != null && mat.diffuseColorMap == null)
            ci.cancel();
    }
}
