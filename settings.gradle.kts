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
  val nexusUsername: String by lazy { System.getenv("NEXUS_USERNAME") ?: settings.providers.gradleProperty("nexusUsername").get() }
  val nexusPassword: String by lazy { System.getenv("NEXUS_PASSWORD") ?: settings.providers.gradleProperty("nexusPassword").get() }
  val nexusBaseUrl: String by lazy { System.getenv("NEXUS_BASE_URL") ?: "https://nexus-proxy-prd.soma.salesforce.com/nexus/content" }

  repositories {
    mavenCentral()
    maven {
      name = "NexusPublic"
      url = uri("$nexusBaseUrl/groups/public")
      credentials {
        username = nexusUsername
        password = nexusPassword
      }
    }
  }
  versionCatalogs {
    create("libs") {
      library("hamcrest-core", "org.hamcrest:hamcrest:2.2")
      library("hamcrest-date", "org.exparity:hamcrest-date:2.0.8")
      library("java-vavr", "io.vavr:vavr:0.10.4")
      library("kotlin-vavr", "io.vavr:vavr-kotlin:0.10.2")
      library("jetbrains-annotations", "org.jetbrains:annotations:23.0.0")
    }
  }
}

rootProject.name = "vader-root"
include("matchers")
include("specs")
include("vader")

plugins {
  id("com.gradle.enterprise") version ("3.9")
}

gradleEnterprise {
  server = "https://gradleenterprise.eng.sfdc.net"
  buildScan {
    publishAlways()
  }
}
