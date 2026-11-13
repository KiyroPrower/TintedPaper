import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.24"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        // Dependency source only: hosts ConfigurationMaster-API and the spark dev builds the server depends on.
        maven("https://maven.leafmc.one/snapshots/")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.isFork = true
        options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
    }
    tasks.withType<Javadoc>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test>().configureEach {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

}

// Keep patch numbers, contents and author credits; normalize only inherited filename branding.
val brandedPatchDirectories = mapOf(
    "rebuildPaperApiFeaturePatches" to file("tintedpaper-api/paper-patches/features"),
    "rebuildMinecraftFeaturePatches" to file("tintedpaper-server/minecraft-patches/features"),
    "rebuildPaperServerFeaturePatches" to file("tintedpaper-server/paper-patches/features"),
)
allprojects {
    tasks.matching { it.name in brandedPatchDirectories.keys }.configureEach {
        val patchDirectory = brandedPatchDirectories.getValue(name)
        doLast {
            for (patch in patchDirectory.listFiles().orEmpty()) {
                val brandedName = patch.name.replace(Regex("(?i)leaf-"), "tintedpaper-")
                if (brandedName != patch.name) {
                    java.nio.file.Files.move(patch.toPath(), patch.toPath().resolveSibling(brandedName))
                }
            }
        }
    }
}

project(":tintedpaper-server") {
    apply(from = rootProject.file("gradle/patch-editing.gradle.kts"))
}

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperCommit")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("tintedpaper-server/build.gradle.kts")
            patchFile = file("tintedpaper-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("tintedpaper-api/build.gradle.kts")
            patchFile = file("tintedpaper-api/build.gradle.kts.patch")
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("tintedpaper-api/paper-patches")
            outputDir = file("paper-api")
        }
    }
}
