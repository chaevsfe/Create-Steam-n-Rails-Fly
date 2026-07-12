import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import javax.imageio.ImageIO

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
    `maven-publish`
}

group = property("maven_group") as String
version = "${property("mod_version")}+fabric-mc${property("minecraft_version")}"

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.blamejared.com/")
    maven("https://maven.maxhenkel.de/repository/public")
    maven("https://jitpack.io/") {
        content { includeGroupByRegex("com\\.github.*") }
    }
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
}

loom {
    accessWidenerPath.set(file("common/src/main/resources/railways.accesswidener"))
    mods {
        create("railways") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        java.setSrcDirs(listOf("common/src/main/java", "fabric/src/main/java"))
        java.exclude(
            "com/railwayteam/railways/base/data/lang/**",
            "com/railwayteam/railways/base/data/RailwaysHatOffsetGenerator.java",
            "com/railwayteam/railways/base/data/recipe/EnumRecipeList.java",
            "com/railwayteam/railways/base/data/recipe/RailwaysMechanicalCraftingRecipeGen.java",
            "com/railwayteam/railways/base/data/recipe/RailwaysSequencedAssemblyRecipeBuilder.java",
            "com/railwayteam/railways/base/data/recipe/RailwaysSequencedAssemblyRecipeGen.java",
            "com/railwayteam/railways/base/data/recipe/RailwaysStandardRecipeGen.java",
            "com/railwayteam/railways/base/data/recipe/processing/**",
            "com/railwayteam/railways/mixin/AccessorTrainPacket.java",
            "com/railwayteam/railways/mixin/Mixin*.java",
            "com/railwayteam/railways/mixin/client/**",
            "com/railwayteam/railways/mixin/conductor_possession/**",
            "com/railwayteam/railways/mixin/compat/voicechat/**",
            "com/railwayteam/railways/mixin/AccessorIngredient\$TagValue.java",
            "com/railwayteam/railways/content/custom_tracks/casing/RuntimeFakePartialModel.java",
            "com/railwayteam/railways/content/custom_tracks/casing/SpriteCopyingBakedModel.java",
            "com/railwayteam/railways/content/conductor/ConductorElytraLayer.java",
            "com/railwayteam/railways/content/conductor/ConductorSecondaryHeadLayer.java",
            "com/railwayteam/railways/content/bogey_menu/components/**",
            "com/railwayteam/railways/registry/advancement/**",
            // Keep the production Shadow Realm command while the unrelated dev/admin command
            // suite is still being ported to 26.2.
            "com/railwayteam/railways/registry/commands/ClearCapCacheCommand.java",
            "com/railwayteam/railways/registry/commands/ClearCasingCacheCommand.java",
            "com/railwayteam/railways/registry/commands/ConductorDemoCommand.java",
            "com/railwayteam/railways/registry/commands/CountPaintCommand.java",
            "com/railwayteam/railways/registry/commands/FillPaintCommand.java",
            "com/railwayteam/railways/registry/commands/IdentifyTrainCommand.java",
            "com/railwayteam/railways/registry/commands/MixinAuditCommand.java",
            "com/railwayteam/railways/registry/commands/PalettesDemoCommand.java",
            "com/railwayteam/railways/registry/commands/ReloadCasingCollisionCommand.java",
            "com/railwayteam/railways/registry/commands/ReloadCreativeTabsCommand.java",
            "com/railwayteam/railways/registry/commands/ReloadDevCapesCommand.java",
            "com/railwayteam/railways/registry/commands/SplitTrainCommand.java",
            "com/railwayteam/railways/registry/commands/TrackDemoCommand.java",
            "com/railwayteam/railways/registry/commands/TrainInfoCommand.java",
            "com/railwayteam/railways/util/client/ClientTextUtils.java",
            "com/railwayteam/railways/util/DebugRendererExtensions.java",
            "com/railwayteam/railways/base/data/fabric/CRTagGenImpl.java",
            "com/railwayteam/railways/base/data/fabric/GeneratedEntriesProvider.java",
            "com/railwayteam/railways/compat/emi/fabric/**",
            "com/railwayteam/railways/base/data/recipe/fabric/**",
            "com/railwayteam/railways/content/buffer/headstock/fabric/CopycatHeadstockModel.java",
            "com/railwayteam/railways/content/palettes/boiler/fabric/BoilerBigOutlinesImpl.java",
            "com/railwayteam/railways/content/palettes/boiler/fabric/BoilerBlockPlacementHelperImpl.java",
            "com/railwayteam/railways/content/palettes/boiler/fabric/ObjModelBuilder.java",
            "com/railwayteam/railways/fabric/events/ClientEventsFabric.java",
            "com/railwayteam/railways/fabric/mixin/**",
        )
        resources.setSrcDirs(
            listOf(
                "common/src/main/resources",
                "common/src/generated/resources",
                "fabric/src/main/resources",
            )
        )
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("maven.modrinth:create-fly:${property("create_fabric_version")}")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:${property("voicechat_api_version")}")
    compileOnly("mezz.jei:jei-${property("jei_fabric_version")}")

    annotationProcessor("io.github.llamalad7:mixinextras-common:${property("mixin_extras_version")}")
    implementation(include("io.github.llamalad7:mixinextras-fabric:${property("mixin_extras_version")}")!!)
}

val connectedTextureResourceRoots = listOf(
    file("common/src/main/resources"),
    file("common/src/generated/resources"),
    file("fabric/src/main/resources"),
)
val generatedConnectedTextureResources = layout.buildDirectory.dir("generated/connected-texture-resources")

// Data-pack format 48+ uses singular registry/resource directories. Keep the
// checked-in legacy generated data unchanged and normalize it only in the
// processed resource output consumed by Minecraft 26.2.
val legacyDatapackPathRewrites = linkedMapOf(
    Regex("^(data/[^/]+)/advancements/") to "${'$'}1/advancement/",
    Regex("^(data/[^/]+)/functions/") to "${'$'}1/function/",
    Regex("^(data/[^/]+)/item_modifiers/") to "${'$'}1/item_modifier/",
    Regex("^(data/[^/]+)/loot_tables/") to "${'$'}1/loot_table/",
    Regex("^(data/[^/]+)/predicates/") to "${'$'}1/predicate/",
    Regex("^(data/[^/]+)/recipes/") to "${'$'}1/recipe/",
    Regex("^(data/[^/]+)/structures/") to "${'$'}1/structure/",
    Regex("^(data/[^/]+)/tags/banner_patterns/") to "${'$'}1/tags/banner_pattern/",
    Regex("^(data/[^/]+)/tags/blocks/") to "${'$'}1/tags/block/",
    Regex("^(data/[^/]+)/tags/damage_types/") to "${'$'}1/tags/damage_type/",
    Regex("^(data/[^/]+)/tags/enchantments/") to "${'$'}1/tags/enchantment/",
    Regex("^(data/[^/]+)/tags/entity_types/") to "${'$'}1/tags/entity_type/",
    Regex("^(data/[^/]+)/tags/fluids/") to "${'$'}1/tags/fluid/",
    Regex("^(data/[^/]+)/tags/functions/") to "${'$'}1/tags/function/",
    Regex("^(data/[^/]+)/tags/game_events/") to "${'$'}1/tags/game_event/",
    Regex("^(data/[^/]+)/tags/instruments/") to "${'$'}1/tags/instrument/",
    Regex("^(data/[^/]+)/tags/items/") to "${'$'}1/tags/item/",
    Regex("^(data/[^/]+)/tags/painting_variants/") to "${'$'}1/tags/painting_variant/",
    Regex("^(data/[^/]+)/tags/point_of_interest_types/") to "${'$'}1/tags/point_of_interest_type/",
    Regex("^(data/[^/]+)/tags/potions/") to "${'$'}1/tags/potion/",
)

fun normalizeDatapackPath(path: String): String =
    legacyDatapackPathRewrites.entries.fold(path) { normalized, (legacy, current) ->
        normalized.replace(legacy, current)
    }

// Create Fly 26.2 loads dynamic-registry entries below the registry key's
// path directly. Railway's generated paint projectile still carries the old
// extra registry-namespace segment (`create/`) in front of that path.
val legacyPaintProjectileRegistryPath = Regex("^data/([^/]+)/create/potato_projectile/(.+)$")

fun normalizePaintProjectileRegistryPath(path: String): String =
    path.replace(legacyPaintProjectileRegistryPath, "data/${'$'}1/potato_projectile/${'$'}2")

// Fabric convention tags v2 groups material/color variants below their tag
// family. Railway's generated data still targets the legacy flat names; keep
// those checked-in resources intact and rewrite only processResources output.
val legacyFabricConventionTagRewrites = buildMap {
    listOf(
        "black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray",
        "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow",
    ).forEach { color -> put("#c:${color}_dyes", "#c:dyes/$color") }

    put("#c:colorless_glass", "#c:glass_blocks/colorless")
    listOf("brass", "copper", "iron").forEach { material ->
        put("#c:${material}_ingots", "#c:ingots/$material")
    }
    listOf("brass", "iron", "zinc").forEach { material ->
        put("#c:${material}_nuggets", "#c:nuggets/$material")
    }
    listOf("brass", "iron").forEach { material ->
        put("#c:${material}_plates", "#c:plates/$material")
    }
    put("#c:string", "#c:strings")
    put("#c:workbench", "#c:player_workstations/crafting_tables")
}

val fabricTrackRodTags = listOf(
    "#c:rods/wrought_iron",
    "#c:rods/zinc",
)

// Compat tracks are registered only when their owning mod is present. Keep the
// generated loot-table sources loader-neutral and attach Fabric 6.1 resource
// conditions only to the processed output.
val compatTrackLootTableModIds = linkedMapOf(
    "track_biomesoplenty_" to "biomesoplenty",
    "track_blue_skies_" to "blue_skies",
    "track_byg_" to "byg",
    "track_create_dd_" to "create_dd",
    "track_hexcasting_" to "hexcasting",
    "track_natures_spirit_" to "natures_spirit",
    "track_quark_" to "quark",
    "track_tfc_" to "tfc",
    "track_twilightforest_" to "twilightforest",
)

// These blocks are implementation details without block items. Railway's
// official generated tables use minecraft:air as a no-drop sentinel, which is
// rejected by Minecraft 26.2; omit only their processed loot tables instead.
val internalNoDropBlockLootTables = setOf(
    "copycat_headstock_bars",
    "smokestack_coalburner_extension",
    "smokestack_long_extension",
    "smokestack_oilburner_extension",
    "smokestack_streamlined_extension",
    "smokestack_woodburner_extension",
)

// Keep this list in lockstep with PalettesColor. Sandy pitchers use water and
// netherite is the projectile fallback, so every listed color is still valid
// paint metadata even when it has no standalone dye recipe.
val paintPaletteColors = listOf(
    "netherite", "brown", "maroon", "red", "vermilion", "orange", "granite", "dripstone",
    "ochrum", "yellow", "chartreuse", "olive_green", "lime", "green", "pine_green", "cyan",
    "sea_green", "turquoise", "light_blue", "blue", "royal_blue", "purple", "magenta", "pink",
    "white", "diorite", "limestone", "light_gray", "tuff", "gray", "scorchia", "black",
)

fun decodePaintColorSequence(encoded: String, colors: List<String>): List<List<String>> {
    if (encoded.isEmpty()) return listOf(emptyList())

    return colors.flatMap { color ->
        when {
            encoded == color -> listOf(listOf(color))
            encoded.startsWith("${color}_") -> decodePaintColorSequence(encoded.removePrefix("${color}_"), colors)
                .map { suffix -> listOf(color) + suffix }
            else -> emptyList()
        }
    }
}

// Create Fly 26.2 stitches every connected-texture variant as an individual
// sprite. Railway's source art deliberately stays in the legacy spritesheet
// format; split the sheets into the indices requested by the 26.2 CT types.
val generateConnectedTextureSprites = tasks.register("generateConnectedTextureSprites") {
    val connectedTextureSheets = connectedTextureResourceRoots.map { root ->
        fileTree(root) { include("**/*_connected.png") }
    }

    inputs.files(connectedTextureSheets)
        .withPropertyName("connectedTextureSheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("connectedTextureLayout", "create-fly-26.2-v1")
    outputs.dir(generatedConnectedTextureResources)

    doLast {
        val omniTileIndexes = listOf(
            1, 2, 3,
            8, 9, 10, 11, 12, 13,
            16, 17, 18, 19, 20, 21,
            24, 25, 26, 27, 28, 29, 30,
            32, 33, 34, 35, 36, 37, 38,
            40, 41, 42, 43, 44, 45, 46,
            48, 49, 50, 51, 52, 53, 54,
            56, 57, 58,
        )
        val rectangleTileIndexes = (0..11).toList() + (13..15).toList()
        val axisTileIndexes = listOf(1, 2, 3)

        val layouts = buildMap<String, Pair<Int, List<Int>>> {
            listOf(
                "slashed_connected",
                "riveted_connected",
                "vent_connected",
                "wrapped_slashed_connected",
                "copper_wrapped_slashed_connected",
                "iron_wrapped_slashed_connected",
            ).forEach { put(it, 8 to omniTileIndexes) }

            listOf(
                "fuel_tank_connected",
                "fuel_tank_top_connected",
                "fuel_tank_inner_connected",
            ).forEach { put(it, 4 to rectangleTileIndexes) }

            listOf(
                "riveted_pillar_side_connected",
                "tank_side_connected",
                "wrapped_tank_side_connected",
                "copper_wrapped_tank_side_connected",
                "iron_wrapped_tank_side_connected",
                "boiler_side_connected",
                "wrapped_boiler_side_connected",
                "copper_wrapped_boiler_side_connected",
                "iron_wrapped_boiler_side_connected",
                "round_pane_window_connected",
                "single_pane_window_connected",
                "two_pane_window_connected",
                "four_pane_window_connected",
            ).forEach { put(it, 2 to axisTileIndexes) }
        }

        val outputRoot = generatedConnectedTextureResources.get().asFile
        delete(outputRoot)

        var sheetCount = 0
        var spriteCount = 0
        connectedTextureResourceRoots.forEach { resourceRoot ->
            if (!resourceRoot.isDirectory) return@forEach

            fileTree(resourceRoot) { include("**/*_connected.png") }
                .files
                .sortedBy { it.invariantSeparatorsPath }
                .forEach { sheetFile ->
                    val (gridSize, tileIndexes) = layouts[sheetFile.nameWithoutExtension]
                        ?: throw GradleException("Unknown connected-texture sheet layout: $sheetFile")
                    val sheet = ImageIO.read(sheetFile)
                        ?: throw GradleException("Could not decode connected-texture sheet: $sheetFile")

                    if (sheet.width % gridSize != 0 || sheet.height % gridSize != 0 ||
                        sheet.width / gridSize != sheet.height / gridSize
                    ) {
                        throw GradleException(
                            "Connected-texture sheet $sheetFile must contain a $gridSize x $gridSize grid of square tiles, " +
                                "but is ${sheet.width} x ${sheet.height}",
                        )
                    }

                    val tileSize = sheet.width / gridSize
                    val relativeSheet = resourceRoot.toPath().relativize(sheetFile.toPath()).toString()
                    val spriteDirectory = outputRoot.resolve(relativeSheet.removeSuffix(".png"))
                    spriteDirectory.mkdirs()

                    tileIndexes.forEachIndexed { index, sourceTileIndex ->
                        val tileX = sourceTileIndex % gridSize
                        val tileY = sourceTileIndex / gridSize
                        val outputFile = spriteDirectory.resolve("${index + 1}.png")
                        val tile = sheet.getSubimage(tileX * tileSize, tileY * tileSize, tileSize, tileSize)
                        if (!ImageIO.write(tile, "png", outputFile)) {
                            throw GradleException("No PNG writer is available for $outputFile")
                        }
                        spriteCount++
                    }
                    sheetCount++
                }
        }

        logger.lifecycle("Generated $spriteCount Create Fly 26.2 CT sprites from $sheetCount Railway sheets")
    }
}

tasks.processResources {
    dependsOn(generateConnectedTextureSprites)
    from(generatedConnectedTextureResources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("minecraft_version"),
        "minecraft_dependency_version" to project.property("minecraft_dependency_version"),
        "fabric_api_version" to project.property("fabric_api_version"),
        "fabric_loader_version" to project.property("fabric_loader_version"),
        "create_fabric_version" to project.property("create_fabric_version"),
        "create_fabric_version_range" to project.property("create_fabric_version_range"),
    )
    inputs.properties(properties)
    inputs.property("railwayFabricConventionTagSchema", "fabric-convention-tags-v2-26.2-v1")
    inputs.property("railwayAdvancementSchema", "minecraft-26.2-v1")
    inputs.property("railwayPaintFluidComponents", "minecraft-26.2-custom-data-v1")
    inputs.property("railwayPaintProjectileRegistryPath", "create-fly-26.2-v1")
    inputs.property("railwayCompatTrackLootConditions", "fabric-resource-conditions-6.1-v1")
    inputs.property("railwayCompatTrackLootConditionMapping", compatTrackLootTableModIds)
    inputs.property("railwayInternalNoDropLootTables", internalNoDropBlockLootTables.sorted())

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
    eachFile {
        if (path.startsWith("data/")) {
            path = normalizeDatapackPath(path)
            path = normalizePaintProjectileRegistryPath(path)
        }
    }

    doLast {
        val outputRoot = destinationDir
        val legacyPaintProjectileFiles = fileTree(outputRoot) {
            include("data/*/create/potato_projectile/**/*.json")
        }.files
        if (legacyPaintProjectileFiles.isNotEmpty()) {
            throw GradleException(
                "Legacy Create potato-projectile registry paths survived processing: " +
                    legacyPaintProjectileFiles.sortedBy(File::getPath).joinToString(),
            )
        }

        val paintProjectileType = outputRoot.resolve("data/railways/potato_projectile/type/paint_pitcher.json")
        if (!paintProjectileType.isFile) {
            throw GradleException(
                "Processed Railway paint projectile type is missing from the Create Fly 26.2 registry path: " +
                    paintProjectileType,
            )
        }

        val paintMixingRoot = outputRoot.resolve("data/railways/recipe/mixing")
        if (!paintMixingRoot.isDirectory) {
            throw GradleException("Processed Railway mixing-recipe directory is missing: $paintMixingRoot")
        }

        var paintIngredientStacks = 0
        var paintResultStacks = 0
        var paintRecipes = 0

        fileTree(paintMixingRoot) { include("**/*.json") }.files.sortedBy(File::getPath).forEach { recipeFile ->
            @Suppress("UNCHECKED_CAST")
            val recipe = JsonSlurper().parse(recipeFile) as? MutableMap<String, Any?>
                ?: throw GradleException("Mixing recipe root must be a JSON object: $recipeFile")

            fun objectList(key: String): List<MutableMap<String, Any?>> {
                val values = recipe[key] as? List<*> ?: return emptyList()
                return values.map { value ->
                    @Suppress("UNCHECKED_CAST")
                    value as? MutableMap<String, Any?>
                        ?: throw GradleException("Mixing recipe '$key' entry must be an object: $recipeFile")
                }
            }

            val paintIngredients = objectList("fluid_ingredients")
                .filter { ingredient -> ingredient["fluid"] == "railways:paint" }
            val paintResults = objectList("fluid_results")
                .filter { result -> result["id"] == "railways:paint" }
            if (paintIngredients.isEmpty() && paintResults.isEmpty()) {
                if (recipeFile.readText(Charsets.UTF_8).contains("railways:paint")) {
                    throw GradleException("Unrecognized paint stack shape in mixing recipe: $recipeFile")
                }
                return@forEach
            }

            val relative = paintMixingRoot.toPath().relativize(recipeFile.toPath())
                .toString().replace('\\', '/')
            val stem = recipeFile.nameWithoutExtension
            val ingredientColors: List<String>
            val resultColor: String?

            when {
                relative.startsWith("palettes/dyeing/") -> {
                    if (paintResults.isNotEmpty()) {
                        throw GradleException("Dyeing recipe unexpectedly produces paint: $relative")
                    }
                    val matches = paintPaletteColors.filter { color ->
                        stem == color || stem.startsWith("${color}_")
                    }
                    if (matches.size != 1) {
                        throw GradleException(
                            "Could not infer one exact paint color from dyeing recipe '$relative': " +
                                matches.joinToString(),
                        )
                    }
                    ingredientColors = List(paintIngredients.size) { matches.single() }
                    resultColor = null
                }

                relative.startsWith("palettes/dye/") -> {
                    resultColor = stem.substringBefore("_from_")
                    if (resultColor !in paintPaletteColors) {
                        throw GradleException("Unknown paint result color '$resultColor' in recipe: $relative")
                    }

                    ingredientColors = if (paintIngredients.isEmpty()) {
                        emptyList()
                    } else {
                        if (!stem.contains("_from_")) {
                            throw GradleException("Paint-consuming dye recipe has no exact '_from_' colors: $relative")
                        }
                        val encodedInputs = stem.substringAfter("_from_")
                        val decodings = decodePaintColorSequence(encodedInputs, paintPaletteColors)
                            .filter { colors -> colors.size == paintIngredients.size }
                        if (decodings.size != 1) {
                            throw GradleException(
                                "Ambiguous paint input colors '$encodedInputs' in recipe '$relative': " +
                                    decodings.joinToString(),
                            )
                        }
                        decodings.single()
                    }
                }

                else -> throw GradleException("Paint stack is outside exact palettes dye paths: $relative")
            }

            fun applyColor(stack: MutableMap<String, Any?>, color: String, role: String) {
                val components = linkedMapOf<String, Any?>(
                    "minecraft:custom_data" to linkedMapOf("Color" to color),
                )
                val existing = stack["components"]
                if (existing != null && existing != components) {
                    throw GradleException(
                        "Conflicting paint components for $role '$color' in $relative: $existing",
                    )
                }
                stack["components"] = components
            }

            paintIngredients.zip(ingredientColors).forEach { (ingredient, color) ->
                applyColor(ingredient, color, "ingredient")
            }
            paintResults.forEach { result ->
                applyColor(
                    result,
                    resultColor ?: throw GradleException("Paint result has no inferred color: $relative"),
                    "result",
                )
            }

            recipeFile.writeText(
                JsonOutput.prettyPrint(JsonOutput.toJson(recipe)) + "\n",
                Charsets.UTF_8,
            )
            paintIngredientStacks += paintIngredients.size
            paintResultStacks += paintResults.size
            paintRecipes++
        }

        if (paintIngredientStacks != 964 || paintResultStacks != 40 || paintRecipes != 970) {
            throw GradleException(
                "Unexpected Railway paint recipe coverage after component migration: " +
                    "$paintIngredientStacks ingredients, $paintResultStacks results, $paintRecipes recipes",
            )
        }
        logger.lifecycle(
            "Attached exact Color custom_data to $paintIngredientStacks paint ingredients and " +
                "$paintResultStacks paint results across $paintRecipes mixing recipes",
        )

        val railwayTagFiles = fileTree(outputRoot) {
            include("data/railways/tags/item/**/*.json")
            include("data/railways/tags/block/**/*.json")
        }.files.sortedBy(File::getPath)

        var rewrittenTagFiles = 0
        var rewrittenTagIds = 0
        railwayTagFiles.forEach { tagFile ->
            val original = tagFile.readText()
            var normalized = original
            legacyFabricConventionTagRewrites.forEach { (legacy, current) ->
                val quotedLegacy = "\"$legacy\""
                val quotedCurrent = "\"$current\""
                while (normalized.contains(quotedLegacy)) {
                    normalized = normalized.replaceFirst(quotedLegacy, quotedCurrent)
                    rewrittenTagIds++
                }
            }

            if (normalized != original) {
                tagFile.writeText(normalized)
                rewrittenTagFiles++
            }
        }

        val trackRodsTag = outputRoot.resolve("data/railways/tags/item/internal/track_rods.json")
        if (!trackRodsTag.isFile) {
            throw GradleException("Processed Railway track-rods tag is missing: $trackRodsTag")
        }

        var trackRodsContent = trackRodsTag.readText()
        val missingTrackRodTags = fabricTrackRodTags.filterNot { current ->
            trackRodsContent.contains("\"$current\"")
        }
        if (missingTrackRodTags.isNotEmpty()) {
            val valuesMarker = "  \"values\": ["
            if (!trackRodsContent.contains(valuesMarker)) {
                throw GradleException("Could not augment malformed Railway track-rods tag: $trackRodsTag")
            }

            val lineSeparator = if (trackRodsContent.contains("\r\n")) "\r\n" else "\n"
            val fabricEntries = missingTrackRodTags.joinToString(",$lineSeparator") { current ->
                """    {
      "id": "$current",
      "required": false
    }""".replace("\n", lineSeparator)
            }
            trackRodsContent = trackRodsContent.replaceFirst(
                valuesMarker,
                "$valuesMarker$lineSeparator$fabricEntries,",
            )
            trackRodsTag.writeText(trackRodsContent)
        }

        val staleConventionTags = railwayTagFiles.flatMap { tagFile ->
            val content = tagFile.readText()
            legacyFabricConventionTagRewrites.keys
                .filter { legacy -> content.contains("\"$legacy\"") }
                .map { legacy ->
                    val relative = outputRoot.toPath().relativize(tagFile.toPath()).toString().replace('\\', '/')
                    "$relative: $legacy"
                }
        }.sorted()

        if (staleConventionTags.isNotEmpty()) {
            throw GradleException(
                "Legacy Fabric convention tag IDs remain after processResources:\n" +
                    staleConventionTags.joinToString("\n"),
            )
        }

        val missingProcessedTrackRodTags = fabricTrackRodTags.filterNot { current ->
            trackRodsTag.readText().contains("\"$current\"")
        }
        if (missingProcessedTrackRodTags.isNotEmpty()) {
            throw GradleException(
                "Processed Railway track-rods tag is missing Fabric convention entries: " +
                    missingProcessedTrackRodTags.joinToString(),
            )
        }

        logger.lifecycle(
            "Normalized $rewrittenTagIds legacy Fabric convention tag IDs in $rewrittenTagFiles Railway tag files; " +
                "added ${missingTrackRodTags.size} Fabric track-rod tags",
        )

        val railwayAdvancementFiles = fileTree(outputRoot) {
            include("data/railways/advancement/*.json")
            include("data/railways/advancement/**/*.json")
        }.files.sortedBy(File::getPath)

        var rewrittenAdvancementIcons = 0
        var rewrittenAdvancementBackgrounds = 0
        var rewrittenAdvancementTriggers = 0
        var rewrittenItemIdPredicates = 0
        var rewrittenItemTagPredicates = 0

        lateinit var normalizeAdvancementNode: (Any?, Boolean) -> Any?
        normalizeAdvancementNode = { node, itemPredicate ->
            when (node) {
                is Map<*, *> -> {
                    val normalized = linkedMapOf<String, Any?>()
                    node.forEach { (rawKey, rawValue) ->
                        val key = rawKey as? String
                            ?: throw GradleException("Advancement JSON contains a non-string object key: $rawKey")
                        var normalizedValue = if (
                            key == "items" && rawValue is List<*> && rawValue.any { it is Map<*, *> }
                        ) {
                            rawValue.map { normalizeAdvancementNode(it, true) }
                        } else {
                            normalizeAdvancementNode(rawValue, false)
                        }

                        if (key == "trigger" && normalizedValue == "railways:strange_tea_builtin") {
                            normalizedValue = "minecraft:impossible"
                            rewrittenAdvancementTriggers++
                        }
                        normalized[key] = normalizedValue
                    }

                    if (itemPredicate) {
                        val legacyTag = normalized.remove("tag")
                        if (legacyTag != null) {
                            if (legacyTag !is String || normalized.containsKey("items")) {
                                throw GradleException("Unsupported legacy item tag predicate: $normalized")
                            }
                            normalized["items"] = "#$legacyTag"
                            rewrittenItemTagPredicates++
                        }

                        val items = normalized["items"]
                        if (items is List<*> && items.all { it is String }) {
                            if (items.size != 1) {
                                throw GradleException("Unsupported legacy multi-item predicate: $items")
                            }
                            normalized["items"] = items.single()
                            rewrittenItemIdPredicates++
                        }
                    }

                    normalized
                }

                is List<*> -> node.map { normalizeAdvancementNode(it, false) }
                else -> node
            }
        }

        railwayAdvancementFiles.forEach { advancementFile ->
            val parsed = JsonSlurper().parse(advancementFile)
            @Suppress("UNCHECKED_CAST")
            val advancement = normalizeAdvancementNode(parsed, false) as? MutableMap<String, Any?>
                ?: throw GradleException("Advancement root must be a JSON object: $advancementFile")
            @Suppress("UNCHECKED_CAST")
            val display = advancement["display"] as? MutableMap<String, Any?>
            @Suppress("UNCHECKED_CAST")
            val icon = display?.get("icon") as? MutableMap<String, Any?>

            val legacyIcon = icon?.remove("item")
            if (legacyIcon != null) {
                if (icon.containsKey("id")) {
                    throw GradleException("Advancement icon contains both legacy 'item' and current 'id': $advancementFile")
                }
                icon["id"] = legacyIcon
                rewrittenAdvancementIcons++
            }

            if (display?.get("background") == "railways:textures/gui/advancements.png") {
                display["background"] = "railways:gui/advancements"
                rewrittenAdvancementBackgrounds++
            }

            advancementFile.writeText(
                JsonOutput.prettyPrint(JsonOutput.toJson(advancement)) + "\n",
                Charsets.UTF_8,
            )
        }

        val rootAdvancement = outputRoot.resolve("data/railways/advancement/root.json")
        val strangeTeaAdvancement = outputRoot.resolve("data/railways/advancement/strange_tea.json")
        if (!rootAdvancement.isFile || !strangeTeaAdvancement.isFile) {
            throw GradleException("Processed Railway root or strange-tea advancement is missing")
        }

        val rootAdvancementContent = rootAdvancement.readText(Charsets.UTF_8)
        val strangeTeaAdvancementContent = strangeTeaAdvancement.readText(Charsets.UTF_8)
        val staleAdvancementFiles = railwayAdvancementFiles.filter { advancementFile ->
            val content = advancementFile.readText(Charsets.UTF_8)
            Regex("\\\"items\\\"\\s*:\\s*\\[\\s*\\\"").containsMatchIn(content) ||
                Regex("\\\"tag\\\"\\s*:").containsMatchIn(content) ||
                content.contains("railways:strange_tea_builtin") ||
                content.contains("railways:textures/gui/advancements.png")
        }
        if (staleAdvancementFiles.isNotEmpty() ||
            !rootAdvancementContent.contains("\"background\": \"railways:gui/advancements\"") ||
            !rootAdvancementContent.contains("\"id\": \"railways:handcar\"") ||
            !strangeTeaAdvancementContent.contains("\"id\": \"create:builders_tea\"") ||
            !strangeTeaAdvancementContent.contains("\"trigger\": \"minecraft:impossible\"")
        ) {
            throw GradleException(
                "Railway advancements still use a legacy schema after processResources" +
                    staleAdvancementFiles.joinToString(prefix = "\n", separator = "\n"),
            )
        }

        logger.lifecycle(
            "Normalized ${railwayAdvancementFiles.size} Railway advancements for Minecraft 26.2: " +
                "$rewrittenAdvancementIcons icons, $rewrittenAdvancementBackgrounds backgrounds, " +
                "$rewrittenAdvancementTriggers triggers, $rewrittenItemIdPredicates item IDs, " +
                "$rewrittenItemTagPredicates item tags",
        )

        val railwayBlockLootTableRoot = outputRoot.resolve("data/railways/loot_table/blocks")
        if (!railwayBlockLootTableRoot.isDirectory) {
            throw GradleException("Processed Railway block loot-table directory is missing: $railwayBlockLootTableRoot")
        }

        val railwayBlockLootTables = fileTree(railwayBlockLootTableRoot) {
            include("*.json")
        }.files.sortedBy(File::getName)
        val conditionedCompatFamilies = compatTrackLootTableModIds.keys.associateWith { 0 }.toMutableMap()
        var conditionedCompatLootTables = 0

        railwayBlockLootTables.forEach { lootTableFile ->
            val lootTableId = lootTableFile.nameWithoutExtension
            val matchingCompatFamilies = compatTrackLootTableModIds.keys.filter(lootTableId::startsWith)
            if (matchingCompatFamilies.size > 1) {
                throw GradleException(
                    "Ambiguous compat loot-table family for $lootTableFile: " +
                        matchingCompatFamilies.joinToString(),
                )
            }

            val compatFamily = matchingCompatFamilies.singleOrNull() ?: return@forEach
            val modId = compatTrackLootTableModIds.getValue(compatFamily)
            @Suppress("UNCHECKED_CAST")
            val lootTable = JsonSlurper().parse(lootTableFile) as? MutableMap<String, Any?>
                ?: throw GradleException("Loot-table root must be a JSON object: $lootTableFile")
            if (lootTable.containsKey("fabric:load_conditions")) {
                throw GradleException("Compat loot table already has a Fabric load condition: $lootTableFile")
            }

            lootTable["fabric:load_conditions"] = linkedMapOf(
                "condition" to "fabric:all_mods_loaded",
                "values" to listOf(modId),
            )
            lootTableFile.writeText(
                JsonOutput.prettyPrint(JsonOutput.toJson(lootTable)) + "\n",
                Charsets.UTF_8,
            )
            conditionedCompatFamilies[compatFamily] = conditionedCompatFamilies.getValue(compatFamily) + 1
            conditionedCompatLootTables++
        }

        val emptyCompatFamilies = conditionedCompatFamilies.filterValues { it == 0 }.keys
        if (emptyCompatFamilies.isNotEmpty()) {
            throw GradleException(
                "No processed Railway loot tables matched compat families: " + emptyCompatFamilies.joinToString(),
            )
        }

        val malformedCompatConditions = railwayBlockLootTables.mapNotNull { lootTableFile ->
            val lootTableId = lootTableFile.nameWithoutExtension
            val compatFamily = compatTrackLootTableModIds.keys.singleOrNull(lootTableId::startsWith)
                ?: return@mapNotNull null
            val expectedModId = compatTrackLootTableModIds.getValue(compatFamily)
            @Suppress("UNCHECKED_CAST")
            val lootTable = JsonSlurper().parse(lootTableFile) as? Map<String, Any?>
                ?: return@mapNotNull lootTableFile.path
            @Suppress("UNCHECKED_CAST")
            val condition = lootTable["fabric:load_conditions"] as? Map<String, Any?>
            if (condition?.get("condition") != "fabric:all_mods_loaded" ||
                condition["values"] != listOf(expectedModId)
            ) {
                lootTableFile.path
            } else {
                null
            }
        }
        if (malformedCompatConditions.isNotEmpty()) {
            throw GradleException(
                "Missing or malformed Fabric compat loot-table conditions:\n" +
                    malformedCompatConditions.joinToString("\n"),
            )
        }

        internalNoDropBlockLootTables.sorted().forEach { lootTableId ->
            val lootTableFile = railwayBlockLootTableRoot.resolve("$lootTableId.json")
            if (!lootTableFile.isFile) {
                throw GradleException("Expected internal no-drop loot table is missing: $lootTableFile")
            }
            val content = lootTableFile.readText(Charsets.UTF_8)
            if (!Regex("\\\"name\\\"\\s*:\\s*\\\"minecraft:air\\\"").containsMatchIn(content)) {
                throw GradleException(
                    "Refusing to remove internal loot table without the official minecraft:air sentinel: $lootTableFile",
                )
            }
            if (!lootTableFile.delete()) {
                throw GradleException("Could not remove processed internal no-drop loot table: $lootTableFile")
            }
        }

        logger.lifecycle(
            "Added Fabric load conditions to $conditionedCompatLootTables compat track loot tables across " +
                "${conditionedCompatFamilies.size} mod families; removed " +
                "${internalNoDropBlockLootTables.size} internal no-drop loot tables",
        )

        val legacyOutputs = fileTree(outputRoot) { include("data/**") }
            .files
            .map { outputRoot.toPath().relativize(it.toPath()).toString().replace('\\', '/') }
            .filter { relative -> legacyDatapackPathRewrites.keys.any { it.containsMatchIn(relative) } }
            .sorted()

        if (legacyOutputs.isNotEmpty()) {
            throw GradleException(
                "Legacy plural data-pack paths remain after processResources:\n" +
                    legacyOutputs.joinToString("\n"),
            )
        }

        val missingNormalizedOutputs = connectedTextureResourceRoots
            .filter(File::isDirectory)
            .flatMap { resourceRoot ->
                fileTree(resourceRoot) { include("data/**") }.files.mapNotNull { sourceFile ->
                    val relative = resourceRoot.toPath().relativize(sourceFile.toPath())
                        .toString()
                        .replace('\\', '/')
                    val normalized = normalizeDatapackPath(relative)
                    val intentionallyOmitted = internalNoDropBlockLootTables.any { lootTableId ->
                        normalized == "data/railways/loot_table/blocks/$lootTableId.json"
                    }
                    normalized.takeIf {
                        it != relative && !intentionallyOmitted && !outputRoot.resolve(it).isFile
                    }
                }
            }
            .distinct()
            .sorted()

        if (missingNormalizedOutputs.isNotEmpty()) {
            throw GradleException(
                "Normalized data-pack resources are missing from processResources output:\n" +
                    missingNormalizedOutputs.joinToString("\n"),
            )
        }
    }

    exclude("architectury.common.json")
    exclude("assets/create/**")
    exclude(".cache/**")
    exclude("**/*.bbmodel", "**/*.lnk", "**/*.xcf", "**/*.md", "**/*.txt", "**/*.blend", "**/*.blend1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "1000"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.jar {
    from("LICENSE")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
