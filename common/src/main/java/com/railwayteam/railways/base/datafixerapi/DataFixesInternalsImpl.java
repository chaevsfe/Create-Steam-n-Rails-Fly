/*
 * Copyright 2022 QuiltMC
 * Modified by the Steam 'n' Rails (Railways) team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.railwayteam.railways.base.datafixerapi;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin.AccessorDataFixTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.BiFunction;
import java.util.concurrent.atomic.AtomicBoolean;

@ApiStatus.Internal
public final class DataFixesInternalsImpl extends DataFixesInternals {
    private final @NotNull Schema latestVanillaSchema;

    private DataFixerEntry dataFixer;
    private final AtomicBoolean updateFailureLogged = new AtomicBoolean();
    private final AtomicBoolean newerVersionLogged = new AtomicBoolean();

    public DataFixesInternalsImpl(@NotNull Schema latestVanillaSchema) {
        this.latestVanillaSchema = latestVanillaSchema;

        this.dataFixer = null;
    }
    public void registerFixer(@Range(from = 0, to = Integer.MAX_VALUE) int currentVersion,
                              @NotNull DataFixer dataFixer) {
        if (this.dataFixer != null) {
            throw new IllegalArgumentException("Railways already has a registered data fixer");
        }

        this.dataFixer = new DataFixerEntry(dataFixer, currentVersion);
    }
    public @Nullable DataFixerEntry getFixerEntry() {
        return dataFixer;
    }
    public @NotNull Schema createBaseSchema(@NotNull BiFunction<Integer, Schema, Schema> factory) {
        return factory.apply(0, this.latestVanillaSchema);
    }
    public @NotNull <T> Dynamic<T> updateWithAllFixers(@NotNull DataFixTypes dataFixTypes, @NotNull Dynamic<T> dynamic) {
        return updateWithAllFixers(((AccessorDataFixTypes) (Object) dataFixTypes).railways$getType(), dynamic);
    }
    public @NotNull <T> Dynamic<T> updateWithAllFixers(@NotNull TypeReference rootType, @NotNull Dynamic<T> dynamic) {
        if (dataFixer == null)
            return dynamic;

        int modDataVersion = DataFixesInternals.getModDataVersion(dynamic);
        if (modDataVersion > dataFixer.currentVersion()) {
            if (newerVersionLogged.compareAndSet(false, true))
                Railways.LOGGER.warn(
                    "[Railways DFU] Data version {} is newer than supported version {}; leaving it unchanged",
                    modDataVersion,
                    dataFixer.currentVersion()
                );
            return dynamic;
        }
        if (modDataVersion == dataFixer.currentVersion())
            return dynamic;

        try {
            Dynamic<T> updated = dataFixer.dataFixer().update(
                rootType,
                dynamic,
                modDataVersion,
                dataFixer.currentVersion()
            );
            return updated.set(
                "Railways_DataVersion",
                updated.createInt(dataFixer.currentVersion())
            );
        } catch (RuntimeException | LinkageError exception) {
            if (updateFailureLogged.compareAndSet(false, true))
                Railways.LOGGER.error(
                    "[Railways DFU] A legacy update failed; preserving the original data and disabling no world data",
                    exception
                );
            return dynamic;
        }
    }
    public void addModDataVersions(@NotNull CompoundTag compound) {
        if (dataFixer != null)
            compound.putInt("Railways_DataVersion", dataFixer.currentVersion());
    }
}
