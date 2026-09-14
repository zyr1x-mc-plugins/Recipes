import net.minecrell.pluginyml.paper.PaperPluginDescription
import org.gradle.api.tasks.Sync

plugins {
    kotlin("jvm") version "2.2.21"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "ru.lewis.recipes"
version = "2.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/zyr1x-mc-plugins/Leaf")

        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // CORE
    compileOnly(libs.leaf.api)

    // KOTLIN RUNTIME (нужно резолвить как library, иначе Intrinsics не найдётся на рантайме)
    library(kotlin("stdlib"))

    // TOOLS
    library(libs.guice)
    library(libs.duration.serializer)

    // MINECRAFT TOOLS
    compileOnly(libs.placeholderapi)
    compileOnly(libs.kyori.minimessage)
    compileOnly(libs.invui)
    compileOnly(libs.worldedit)
    compileOnly(libs.worldguard)
    library(libs.litecommands)
    library(libs.sponge.yaml)
    library(libs.sponge.extra.kotlin)
    compileOnly(files("gradle/libs/FancyNpcs-2.11.0.jar"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")

        exclude("kotlin/**")
        exclude("kotlinx/**")
    }
}

// Собирает plugin-jar в единую папку для релиза/раздачи.
val assembleDistribution = tasks.register<Sync>("assembleDistribution") {
    group = "build"
    description = "Copies the plugin jar into build/dist"

    dependsOn(tasks.named("jar"))
    dependsOn(tasks.named("shadowJar"))

    into(layout.buildDirectory.dir("dist"))

    from(tasks.shadowJar.get().archiveFile) {
        rename { "Recipes.jar" }
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
    dependsOn(assembleDistribution)
}

configurations.all {
    resolutionStrategy {
        force(libs.gson)
        force(libs.guava)
    }
}

paper {
    name = "Recipes"
    version = "1.0"
    main = "ru.lewis.recipes.bootstrap.Bootstrap"
    loader = "ru.lewis.recipes.RecipesLoader"
    apiVersion = "1.21"
    author = "Lewis Carrol"

    generateLibrariesJson = true

    serverDependencies {
        register("WorldEdit") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("WorldGuard") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("FancyNpcs") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("PlaceholderAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}

kotlin {
    jvmToolchain(21)
}