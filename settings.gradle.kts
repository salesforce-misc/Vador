pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  val kotlinVersion: String by settings
  val lombokGradlePluginVersion: String by settings
  plugins {
    kotlin("jvm") version kotlinVersion
    id("io.freefair.lombok") version lombokGradlePluginVersion
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      alias("hamcrest-core").to("org.hamcrest:hamcrest:2.2")
      alias("hamcrest-date").to("org.exparity:hamcrest-date:2.0.8")
      alias("java-vavr").to("io.vavr:vavr:0.10.4")
      alias("kotlin-vavr").to("io.vavr:vavr-kotlin:0.10.2")
      alias("jetbrains-annotations").to("org.jetbrains:annotations:22.0.0")
    }
  }
}

rootProject.name = "vader-root"
include("matchers")
include("specs")
include("vader")
