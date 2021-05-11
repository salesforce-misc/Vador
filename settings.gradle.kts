pluginManagement {
    repositories {
        gradlePluginPortal() // This is for other community plugins
        mavenCentral()
    }
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "vader"
