/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

loom {
    accessWidenerPath = file("src/main/resources/railways.accesswidener")
}

architectury {
    common {
        for(p in rootProject.subprojects) {
            if(p != project) {
                this@common.add(p.name)
            }
        }
    }
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    // Compile against Create Fabric in common
    // beware of differences across platforms!
    // dependencies must also be pulled in to minimize problems, from remapping issues to compile errors.
    // All dependencies except Flywheel and Registrate are NOT safe to use!
    // Flywheel and Registrate must also be used carefully due to differences.
    modCompileOnly(rootProject.extra["patchedCreateFlyFiles"]!!)
    compileOnly("com.tterrag.registrate:Registrate:MC1.20-1.3.11")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // required for proper remapping and compiling
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    modCompileOnly("de.maxhenkel.voicechat:voicechat-api:${"voicechat_api_version"()}")
    modCompileOnly("maven.modrinth:simple-voice-chat:fabric-${"voicechat_version"()}")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)
}

tasks.processResources {
    // must be part of primary mod to be findable
    exclude("resourcepacks/")

    // don't add development or to-do files into built jar
    exclude("**/*.bbmodel", "**/*.lnk", "**/*.xcf", "**/*.md", "**/*.txt", "**/*.blend", "**/*.blend1")

    listOf("src/main/resources", "src/generated/resources").forEach { resourceDir ->
        from(resourceDir) {
            include("data/**/tags/items/**")
            eachFile {
                path = path.replace("/tags/items/", "/tags/item/")
            }
            includeEmptyDirs = false
        }
        from(resourceDir) {
            include("data/**/tags/blocks/**")
            eachFile {
                path = path.replace("/tags/blocks/", "/tags/block/")
            }
            includeEmptyDirs = false
        }
    }
}

sourceSets.main {
    java {
        exclude("com/railwayteam/railways/base/data/lang/**")
        exclude("com/railwayteam/railways/base/data/RailwaysHatOffsetGenerator.java")
        exclude("com/railwayteam/railways/base/data/recipe/EnumRecipeList.java")
        exclude("com/railwayteam/railways/base/data/recipe/RailwaysMechanicalCraftingRecipeGen.java")
        exclude("com/railwayteam/railways/base/data/recipe/RailwaysSequencedAssemblyRecipeBuilder.java")
        exclude("com/railwayteam/railways/base/data/recipe/RailwaysSequencedAssemblyRecipeGen.java")
        exclude("com/railwayteam/railways/base/data/recipe/RailwaysStandardRecipeGen.java")
        exclude("com/railwayteam/railways/base/data/recipe/processing/**")
        exclude("com/railwayteam/railways/mixin/AccessorTrainPacket.java")
        exclude("com/railwayteam/railways/mixin/Mixin*.java")
        exclude("com/railwayteam/railways/mixin/MixinTrainPacket.java")
        exclude("com/railwayteam/railways/mixin/client/**")
        exclude("com/railwayteam/railways/mixin/conductor_possession/**")
        exclude("com/railwayteam/railways/mixin/AccessorIngredient\$TagValue.java")
        exclude("com/railwayteam/railways/mixin/MixinAbstractMinecart.java")
        exclude("com/railwayteam/railways/mixin/MixinAbstractMinecart_Type.java")
        exclude("com/railwayteam/railways/mixin/MixinAllBlocks.java")
        exclude("com/railwayteam/railways/mixin/MixinConfigHelper.java")
        exclude("com/railwayteam/railways/mixin/MixinProcessingRecipeSerializer.java")
        exclude("com/railwayteam/railways/mixin/MixinStationEditPacket.java")
        exclude("com/railwayteam/railways/mixin/MixinWalkNodeEvaluator.java")
        exclude("com/railwayteam/railways/content/custom_tracks/casing/RuntimeFakePartialModel.java")
        exclude("com/railwayteam/railways/content/custom_tracks/casing/SpriteCopyingBakedModel.java")
        exclude("com/railwayteam/railways/content/conductor/ConductorElytraLayer.java")
        // ConductorFlagLayer, ConductorRemoteLayer, ConductorToolboxLayer ported to 1.21.11 API
        exclude("com/railwayteam/railways/content/conductor/ConductorSecondaryHeadLayer.java")
        exclude("com/railwayteam/railways/content/bogey_menu/components/**")
        exclude("com/railwayteam/railways/content/animated_flywheel/FlywheelMovementBehaviour.java")
        exclude("com/railwayteam/railways/registry/advancement/**")
        exclude("com/railwayteam/railways/registry/commands/**")
        exclude("com/railwayteam/railways/util/client/ClientTextUtils.java")
        exclude("com/railwayteam/railways/util/DebugRendererExtensions.java")
    }
    resources { // include generated resources in resources
        srcDir("src/generated/resources")
        exclude(".cache/**")
        exclude("assets/create/**")
    }
    blossom.javaSources {
        property("version", "mod_version"())
        property("gitCommit", rootProject.extra["gitHash"].toString())
        property("includeDevCommands", rootProject.extra["includeDevCommands"].toString())
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
