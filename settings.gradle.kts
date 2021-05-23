pluginManagement {
    repositories {
        gradlePluginPortal() // This is for other community plugins
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
    }
}

rootProject.name = "vader"
