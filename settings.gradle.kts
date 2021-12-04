pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  val kotlinVersion: String by settings
  val lombokGradlePluginVersion: String by settings
  plugins {
    kotlin("jvm") version kotlinVersion
    id("io.freefair.lombok") version lombokGradlePluginVersion
  }
}

enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      alias("hamcrest-core").to("org.hamcrest:hamcrest:2.2")
      alias("hamcrest-date").to("org.exparity:hamcrest-date:2.0.7")
      alias("java-vavr").to("io.vavr:vavr:0.10.4")
      alias("kotlin-vavr").to("io.vavr:vavr-kotlin:0.10.2")
      alias("jetbrains-annotations").to("org.jetbrains:annotations:21.0.1")
    }
  }
}

rootProject.name = "vader-root"
include("matchers")
include("specs")
include("vader")
