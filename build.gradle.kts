/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.plugin.ArchitectPluginExtension
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater
import dev.ithundxr.silk.ChangelogText
import me.modmuss50.mpp.ReleaseType

plugins {
    java
    `maven-publish`
    id("architectury-plugin") version "3.5.169"
    id("dev.architectury.loom") version "1.17.487" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.4" apply false // https://github.com/modmuss50/mod-publish-plugin
    id("com.gradleup.shadow") version "9.4.3" apply false
    id("dev.ithundxr.silk") version "0.11.15" // https://github.com/IThundxr/silk
    id("net.kyori.blossom") version "2.1.0" apply false // https://github.com/KyoriPowered/blossom
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8" // https://github.com/JetBrains/gradle-idea-ext-plugin
}

println("Steam 'n' Rails v${"mod_version"()}")

val isRelease = System.getenv("RELEASE_BUILD")?.toBoolean() ?: false
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt()
// whether dev mixins should be stripped, even if it's not a release build
val removeDevMixinAnyway = System.getenv("REMOVE_DEV_MIXIN_ANYWAY")?.toBoolean() ?: false
// whether the build should include dev commands, even in a non-dev environment
val includeDevCommands = !isRelease && System.getenv("INCLUDE_DEV_COMMANDS")?.toBoolean() ?: false
val gitHash = "\"${calculateGitHash() + (if (hasUnstaged()) "-modified" else "")}\""

repositories {
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
}

val patchedCreateFlyJar = layout.projectDirectory.file("gradle/patched-deps/create-fly-${"create_fabric_version"()}-dev-patched.jar")

fun resolveCreateFlyDevJar(): File {
    return configurations.detachedConfiguration(
        dependencies.create("maven.modrinth:create-fly:${"create_fabric_version"()}")
    ).also {
        it.isTransitive = false
    }.singleFile
}

fun patchCreateFlyDevJar(sourceJar: File, outputFile: File) {
    outputFile.parentFile.mkdirs()

    JarFile(sourceJar).use { jar ->
        JarOutputStream(outputFile.outputStream()).use { out ->
            jar.entries().asIterator().forEach { entry ->
                if (entry.isDirectory)
                    return@forEach

                var data = jar.getInputStream(entry).readAllBytes()
                if (entry.name.endsWith(".class"))
                    data = patchCreateFlyMixinDescriptors(entry.name, data)

                out.putNextEntry(JarEntry(entry.name))
                out.write(data)
                out.closeEntry()
            }
        }
    }
}

if (!patchedCreateFlyJar.asFile.isFile) {
    patchCreateFlyDevJar(resolveCreateFlyDevJar(), patchedCreateFlyJar.asFile)
}

val patchCreateFlyDevJarTask = tasks.register("patchCreateFlyDevJar") {
    outputs.file(patchedCreateFlyJar)

    doLast {
        patchCreateFlyDevJar(resolveCreateFlyDevJar(), patchedCreateFlyJar.asFile)
    }
}
val patchedCreateFlyFiles = files(patchedCreateFlyJar).also {
    it.builtBy(patchCreateFlyDevJarTask)
}
extra["patchedCreateFlyFiles"] = patchedCreateFlyFiles

if (!isRelease && removeDevMixinAnyway) {
    println("Removing dev mixins, even though it's not a release build")
}

if (includeDevCommands) {
    println("Including dev commands in build")
}

extra["gitHash"] = gitHash
extra["includeDevCommands"] = includeDevCommands

architectury {
    minecraft = "minecraft_version"()
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    base.archivesName.set("archives_base_name"())
    group = "maven_group"()

    // Formats the mod version to include the loader and Minecraft version.
    // example: 1.0.0+fabric-mc1.19.2

    var gitBranchLabel = "";
    if (!isRelease && "mod_version"().endsWith("-alpha")) {
        // gitBranchLabel should be "-" + the current git branch (replacing any slashes with underscores)
        gitBranchLabel = "-" + calculateGitBranch().replace("/", "_")
    }

    version = "${"mod_version"()}${gitBranchLabel}+${project.name}-mc${"minecraft_version"()}"

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    java {
        withSourcesJar()
    }
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "net.kyori.blossom")

    setupRepositories()

    val capitalizedName =
        project.name.replaceFirstChar { it.uppercase() }

    val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
    loom.apply {
        silentMojangMappingsLicense()
        runs.configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            vmArg("-Dmixin.debug.export=true")
            vmArg("-Dmixin.env.remapRefMap=true")
            vmArg("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
            if (providers.gradleProperty("railways.debugCycleMenu").map { it.toBoolean() }.orElse(false).get()) {
                vmArg("-Drailways.debugCycleMenu=true")
            }
        }
    }

    configurations.configureEach {
        resolutionStrategy {
            force("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
        }
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:${"minecraft_version"()}")
        // layered mappings - Mojmap names, parchment docs and parameters
        "mappings"(loom.layered {
            officialMojangMappings { nameSyntheticMembers = false }
            parchment("org.parchmentmc.data:parchment-${"minecraft_version"()}:${"parchment_version"()}@zip")
        })

        // Used to decompile mixin dumps, needs to be on the classpath
        // Uncomment if you want it to decompile mixin exports, beware it has very verbose logging.
        //implementation("org.vineflower:vineflower:1.10.0")
    }

    publishing {
        publications {
            create<MavenPublication>("maven${capitalizedName}") {
                artifactId = "${"archives_base_name"()}-${project.name}-${"minecraft_version"()}"
                from(components["java"])
            }
        }

        repositories {
            val mavenToken = System.getenv("MAVEN_TOKEN")
            val maven = if (isRelease) "releases" else "snapshots"
            if (mavenToken != null && mavenToken.isNotEmpty()) {
                maven {
                    url = uri("https://maven.ithundxr.dev/${maven}")
                    credentials {
                        username = "railways-github"
                        password = mavenToken
                    }
                }
            }
        }
    }

    // from here down is platform configuration
    if(project.path == ":common") {
        afterEvaluate {
            tasks.named<Jar>("jar") {
                archiveClassifier.set("")
                destinationDirectory = layout.buildDirectory.dir("libs").get()
            }
        }
        return@subprojects
    }

    tasks.withType<org.gradle.api.tasks.bundling.AbstractArchiveTask>().configureEach {
        archiveFileName.set(provider {
            val appendix = archiveAppendix.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            val version = archiveVersion.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            val classifier = archiveClassifier.orNull?.takeIf { it.isNotEmpty() }?.let { "-$it" } ?: ""
            "${archiveBaseName.get()}$appendix$version$classifier.${archiveExtension.get()}"
        })
    }

    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    architectury {
        platformSetupLoomIde()
    }

    val remapJar = tasks.named<RemapJarTask>("remapJar") {
        from("${rootProject.projectDir}/LICENSE")
        val shadowJar = project.tasks.named<ShadowJar>("shadowJar").get()
        inputFile.set(shadowJar.archiveFile)
        injectAccessWidener = true
        dependsOn(shadowJar)
        archiveClassifier = null
        doLast {
            transformJar(outputs.files.singleFile)
        }
    }

    val common: Configuration by configurations.creating
    val shadowCommon: Configuration by configurations.creating
    val development = configurations.maybeCreate("development${capitalizedName}")

    configurations {
        compileOnly.get().extendsFrom(common)
        runtimeOnly.get().extendsFrom(common)
        development.extendsFrom(common)
    }

    dependencies {
        common(project(":common", "namedElements")) { isTransitive = false }
        shadowCommon(project(":common", "transformProduction${capitalizedName}")) { isTransitive = false }
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier = "dev-shadow"
        configurations = listOf(shadowCommon)
        exclude("architectury.common.json")
        destinationDirectory = layout.buildDirectory.dir("devlibs").get()
    }

    tasks.processResources {
        // include packs
        from(project(":common").file("src/main/resources")) {
            include("resourcepacks/")
        }

        // set up properties for filling into metadata
        val properties = mapOf(
                "version" to version,
                "minecraft_version" to "minecraft_version"(),
                "fabric_api_version" to "fabric_api_version"(),
                "fabric_loader_version" to "fabric_loader_version"(),
                "voicechat_api_version" to "voicechat_api_version"(),
                "create_fabric_version" to "create_fabric_version"(),
                "create_fabric_version_range" to "create_fabric_version_range"(),
        )

        inputs.properties(properties)

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    tasks.jar {
        archiveClassifier = "dev"

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    tasks.named<Jar>("sourcesJar") {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    components.getByName<AdhocComponentWithVariants>("java") {
        withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
            skip()
        }
    }

    val releaseType =
        if (version.toString().contains("alpha")) {
            ReleaseType.ALPHA;
        } else if (version.toString().contains("beta")) {
            ReleaseType.BETA;
        } else {
            ReleaseType.STABLE;
        }
    configure<ModPublishExtension> {
        file.set(remapJar.get().archiveFile)
        version.set(project.version.toString())
        changelog = ChangelogText.getChangelogText(rootProject).toString()
        type = releaseType
        displayName = "Steam 'n' Rails ${"mod_version"()} $capitalizedName ${"minecraft_version"()} C${"create_display_version"()}"
        modLoaders.add("fabric")
        modLoaders.add("quilt")

        curseforge {
            projectId = "curseforge_id"()
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = "create-fly"
            }

            requires("fabric-api")
        }

        modrinth {
            projectId = "modrinth_id"()
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = "create-fly"
            }

            requires("fabric-api")
        }
    }
}

fun transformJar(jar: File) {
    val contents = linkedMapOf<String, ByteArray>()
    JarFile(jar).use {
        it.entries().asIterator().forEach { entry ->
            if (!entry.isDirectory) {
                contents[entry.name] = it.getInputStream(entry).readAllBytes()
            }
        }
    }

    jar.delete()

    JarOutputStream(jar.outputStream()).use { out ->
        out.setLevel(Deflater.BEST_COMPRESSION)
        contents.forEach { var (name, data) = it
            if(name.startsWith("architectury_inject_${project.name}_common"))
                return@forEach

            if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                data = (JsonOutput.toJson(JsonSlurper().parse(data)).toByteArray())
            } else if (name.endsWith(".class")) {
                data = transformClass(data)
            }

            out.putNextEntry(JarEntry(name))
            out.write(data)
            out.closeEntry()
        }
        out.finish()
        out.close()
    }
}

fun transformClass(bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)

    // Remove Methods & Field Annotated with @DevEnvMixin
    node.methods.removeIf { methodNode: MethodNode -> removeIfDevMixin(node.name, methodNode.visibleAnnotations) }
    // Disabled as I don't feel ok with people being able to remove these
    //node.fields.removeIf { fieldNode: FieldNode -> removeIfDevMixin(fieldNode.visibleAnnotations) }

    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchCreateFlyMixinDescriptors(entryName: String, bytes: ByteArray): ByteArray {
    val replacements = when (entryName) {
        "com/zurrtum/create/mixin/EntityMixin.class",
        "com/zurrtum/create/client/mixin/EntityMixin.class" -> mapOf(
            "method_5873" to "method_5873(Lnet/minecraft/class_1297;ZZ)Z"
        )
        "com/zurrtum/create/client/mixin/MinecraftClientMixin.class" -> mapOf(
            "method_18096" to "method_18096(Lnet/minecraft/class_437;ZZ)V"
        )
        else -> return bytes
    }

    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)
    var changed = false

    for (method in node.methods) {
        changed = patchMixinAnnotationMethods(method.visibleAnnotations, replacements) || changed
        changed = patchMixinAnnotationMethods(method.invisibleAnnotations, replacements) || changed
    }

    if (!changed)
        return bytes

    return ClassWriter(0).also { node.accept(it) }.toByteArray()
}

fun patchMixinAnnotationMethods(annotations: List<AnnotationNode>?, replacements: Map<String, String>): Boolean {
    if (annotations == null)
        return false

    var changed = false
    for (annotation in annotations) {
        val values = annotation.values ?: continue
        var i = 0
        while (i < values.size - 1) {
            if (values[i] == "method") {
                @Suppress("UNCHECKED_CAST")
                val methods = values[i + 1] as? MutableList<Any>
                if (methods != null) {
                    for (j in methods.indices) {
                        val replacement = replacements[methods[j] as? String]
                        if (replacement != null) {
                            methods[j] = replacement
                            changed = true
                        }
                    }
                }
            }
            i += 2
        }
    }
    return changed
}

fun removeIfDevMixin(nodeName: String, visibleAnnotations: List<AnnotationNode>?): Boolean {
    // Don't remove methods if it's not a GHA build/Release build
    if (!removeDevMixinAnyway && buildNumber == null && !nodeName.lowercase(Locale.ROOT).matches(Regex(".*\\/mixin\\/.*Mixin")))
        return false

    if (visibleAnnotations != null) {
        for (annotationNode in visibleAnnotations) {
            if (annotationNode.desc == "Lcom/railwayteam/railways/annotation/mixin/DevEnvMixin;")
                return true
        }
    }

    return false
}

fun <T> getValueFromAnnotation(annotation: AnnotationNode?, key: String): T? {
    var getNextValue = false

    if (annotation?.values == null) {
        return null
    }

    // Keys and value are stored in successive pairs, search for the key and if found return the following entry
    for (value in annotation.values) {
        if (getNextValue) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        if (value == key) {
            getNextValue = true
        }
    }

    return null
}

tasks.register("railwaysPublish") {
    when (val platform = System.getenv("PLATFORM")) {
        null, "", "fabric" -> {
            dependsOn(":fabric:build", ":fabric:publish", ":fabric:publishMods")
        }
        else -> {
            throw GradleException("Unsupported PLATFORM '$platform'; this project only publishes Fabric builds.")
        }
    }
}

fun Project.setupRepositories() {
    repositories {
        mavenCentral()
        maven("https://maven.createmod.net") // Create, Ponder, Flywheel
        maven("https://modmaven.dev/") // flywheel fabric
        maven("https://maven.shedaniel.me/") // Cloth Config, REI
        maven("https://maven.blamejared.com/") // JEI, Hex Casting
        exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc.data") // Parchment mappings
        exclusiveMaven("https://maven.quiltmc.org/repository/release", "org.quiltmc") // Quilt Mappings
        exclusiveMaven("https://api.modrinth.com/maven", "maven.modrinth") // LazyDFU
        exclusiveMaven("https://cursemaven.com", "curse.maven")
        maven("https://maven.theillusivec4.top/") // Curios
        maven("https://maven.ithundxr.dev/mirror") { // Registrate
            content {
                includeGroup("com.tterrag.registrate")
            }
        }
        maven("https://maven.maxhenkel.de/repository/public") // Simple Voice Chat
        maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
        maven("https://maven.terraformersmc.com/releases/") // Mod Menu, EMI
        maven("https://mvn.devos.one/snapshots/") // Create Fabric, Porting Lib, Milk Lib, Registrate Fabric
        maven("https://mvn.devos.one/releases/") // Porting Lib
        maven("https://maven.cafeteria.dev/releases") // Fake Player API
        exclusiveMaven("https://maven.ladysnake.org/releases", "dev.onyxstudios.cardinal-components-api") // Cardinal Components (Hex Casting dependency)
        maven("https://jitpack.io/") { // Mixin Extras, Fabric ASM
            content {
                includeGroupByRegex("com.github.*")
            }
        }
        maven("$rootDir/local-maven")
    }
}

fun calculateGitHash(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch(_: Throwable) {
        return "unknown"
    }
}

fun calculateGitBranch(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch(_: Throwable) {
        return "unknown"
    }
}

fun hasUnstaged(): Boolean {
    try {
        val output = providers.exec {
            commandLine("git", "status", "--porcelain")
        }
        val result = output.standardOutput.asText.get().replace(Regex("M gradlew(\\.bat)?"), "").trimEnd()
        if (result.isNotEmpty())
            println("Found stageable results:\n${result}\n")
        return result.isNotEmpty()
    }  catch(_: Throwable) {
        return false
    }
}

fun Project.architectury(action: Action<ArchitectPluginExtension>) {
    action.execute(this.extensions.getByType<ArchitectPluginExtension>())
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg groups: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroup(it)
            }
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}

