package com.railwayteam.railways.base.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

public class V4534 extends NamespacedSchema {
    public V4534(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static void register(DataFixerBuilder builder) {
        Schema schema = builder.addSchema(4534, V4534::new);
        builder.addFixer(new AddNewChoices(schema, "Added Railways entities", References.ENTITY));
        builder.addFixer(new AddNewChoices(schema, "Added Railways block entities", References.BLOCK_ENTITY));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        for (String id : new String[] {"railways:conductor", "railways:benchcart", "railways:jukeboxcart"})
            schema.registerSimple(map, id);
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        for (String id : new String[] {"railways:track_buffer_wood_variant", "railways:headstock", "railways:copycat_headstock"})
            schema.register(map, id, () -> DSL.optionalFields("Material", References.BLOCK_STATE.in(schema)));
        return map;
    }
}
