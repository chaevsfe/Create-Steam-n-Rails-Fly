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

architectury.fabric()

loom {
    val common = project(":common")
    accessWidenerPath = common.loom.accessWidenerPath

    runs {
        create("datagen") {
            client()

            name = "Minecraft Data"
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${layout.buildDirectory.dir("generated/datagen").get().asFile}")
            vmArg("-Dfabric-api.datagen.modid=railways")
            vmArg("-Dporting_lib.datagen.existing_resources=${common.file("src/main/resources")}")

            environmentVariable("DATAGEN", "TRUE")
        }
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    // Create - dependencies are added transitively
    modImplementation(rootProject.extra["patchedCreateFlyFiles"]!!)
    compileOnly("com.tterrag.registrate:Registrate:MC1.20-1.3.11")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Fabric ASM (enum extension etc)
    modImplementation("com.github.Chocohead:Fabric-ASM:v2.3") {
        exclude (group = "net.fabricmc.fabric-api")
    }

    include("com.github.Chocohead:Fabric-ASM:v2.3")

    modCompileOnly("dev.emi:emi-fabric:${"emi_version"()}:api") { isTransitive = false }

    // JEI (recipe viewer) - dev runtime only, for testing recipe-viewer compatibility
    if ("enable_jei"().toBoolean()) {
        modLocalRuntime("maven.modrinth:jei:${"jei_fabric_version"()}")
    }

    modCompileOnly("de.maxhenkel.voicechat:voicechat-api:${"voicechat_api_version"()}")

    if ("enable_simple_voice_chat"().toBoolean()) {
        modLocalRuntime("maven.modrinth:simple-voice-chat:fabric-${"voicechat_version"()}")
    }

    // mod compat for tracks
    if ("enable_hexcasting"().toBoolean()) {
        modLocalRuntime("at.petra-k.paucal:paucal-fabric-${"minecraft_version"()}:${"paucal_version"()}")
        modLocalRuntime("at.petra-k.hexcasting:hexcasting-fabric-${"minecraft_version"()}:${"hexcasting_version"()}")
        modLocalRuntime("vazkii.patchouli:Patchouli:${"minecraft_version"()}-${"patchouli_version"()}-FABRIC")
    }

    if ("enable_byg"().toBoolean()) {
        modLocalRuntime("maven.modrinth:biomesyougo:${"byg_version"()}-fabric")
        modLocalRuntime("maven.modrinth:terrablender:${"terrablender_version_fabric"()}")
        modLocalRuntime("maven.modrinth:geckolib:${"geckolib_version_fabric"()}")
        modLocalRuntime("maven.modrinth:corgilib:${"corgilib_version_fabric"()}")
    }

    if ("enable_natures_spirit"().toBoolean()) {
        modLocalRuntime("maven.modrinth:natures-spirit:${"natures_spirit_version"()}")
    }

    if ("enable_tweakeroo"().toBoolean()) {
        modLocalRuntime("curse.maven:tweakeroo-297344:${"tweakeroo_version"()}")
        modLocalRuntime("curse.maven:malilib-303119:${"malilib_version"()}")
    }

    if ("enable_sodium_rubidium"().toBoolean()) {
        modLocalRuntime("maven.modrinth:sodium:${"sodium_version"()}")
        modLocalRuntime("org.joml:joml:1.10.2")
        modLocalRuntime("maven.modrinth:indium:${"indium_version"()}")
    }
    if ("enable_iris"().toBoolean()) {
        modLocalRuntime("maven.modrinth:iris:${"iris_version"()}")
        modLocalRuntime("org.anarres:jcpp:1.4.14")
        modLocalRuntime("io.github.douira:glsl-transformer:2.0.0-pre13")
    }

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)!!
    implementation(include("io.github.llamalad7:mixinextras-fabric:${"mixin_extras_version"()}")!!)!!
}

sourceSets.main {
    java {
        exclude("com/railwayteam/railways/base/data/fabric/CRTagGenImpl.java")
        exclude("com/railwayteam/railways/base/data/fabric/GeneratedEntriesProvider.java")
        exclude("com/railwayteam/railways/compat/emi/fabric/**")
        exclude("com/railwayteam/railways/base/data/recipe/fabric/**")
        exclude("com/railwayteam/railways/content/buffer/fabric/BufferModel.java")
        exclude("com/railwayteam/railways/content/buffer/headstock/fabric/CopycatHeadstockBarsModel.java")
        exclude("com/railwayteam/railways/content/buffer/headstock/fabric/CopycatHeadstockModel.java")
        exclude("com/railwayteam/railways/content/custom_tracks/generic_crossing/fabric/GenericCrossingModel.java")
        exclude("com/railwayteam/railways/content/fuel/psi/**")
        exclude("com/railwayteam/railways/content/fuel/tank/**")
        exclude("com/railwayteam/railways/content/palettes/boiler/fabric/BoilerBigOutlinesImpl.java")
        exclude("com/railwayteam/railways/content/palettes/boiler/fabric/BoilerBlockPlacementHelperImpl.java")
        exclude("com/railwayteam/railways/content/palettes/boiler/fabric/ObjModelBuilder.java")
        exclude("com/railwayteam/railways/content/palettes/painting/fabric/PaintPitcherFluidStorage.java")
        exclude("com/railwayteam/railways/fabric/events/ClientEventsFabric.java")
        exclude("com/railwayteam/railways/fabric/mixin/**")
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
