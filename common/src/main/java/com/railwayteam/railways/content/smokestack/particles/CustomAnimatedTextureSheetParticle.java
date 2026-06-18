/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.content.smokestack.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public abstract class CustomAnimatedTextureSheetParticle extends SingleQuadParticle {
    protected CustomAnimatedTextureSheetParticle(ClientLevel clientLevel, double x, double y, double z, TextureAtlasSprite sprite) {
        super(clientLevel, x, y, z, sprite);
    }

    protected CustomAnimatedTextureSheetParticle(ClientLevel clientLevel, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(clientLevel, x, y, z, xd, yd, zd, sprite);
    }

    protected abstract double getAnimationProgress();

    protected int frameWidthFactor() {
        return 1;
    }

    protected int frameHeightFactor() {
        return 1;
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    protected float getV0() {
        int frames = (this.sprite.contents().height() * frameWidthFactor()) / (this.sprite.contents().width() * frameHeightFactor());
        int frameNumber = (int) (frames * getAnimationProgress());
        return this.sprite.getV((float) frameNumber / frames);
    }

    @Override
    protected float getV1() {
        int frames = (this.sprite.contents().height() * frameWidthFactor()) / (this.sprite.contents().width() * frameHeightFactor());
        int frameNumber = (int) (frames * getAnimationProgress());
        return this.sprite.getV((float) (frameNumber + 1) / frames);
    }
}
