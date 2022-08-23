/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  val kotlinVersion: String by settings
  plugins {
    kotlin("jvm") version kotlinVersion
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      library("hamcrest-core", "org.hamcrest:hamcrest:2.2")
      library("hamcrest-date", "org.exparity:hamcrest-date:2.0.8")
      library("java-vavr", "io.vavr:vavr:0.10.4")
      library("kotlin-vavr", "io.vavr:vavr-kotlin:0.10.2")
      library("jetbrains-annotations", "org.jetbrains:annotations:23.0.0")
      library("typeTools", "net.jodah:typetools:0.6.3")
    }
  }
}

rootProject.name = "vader-root"
include("matchers")
include("specs")
include("vader")
