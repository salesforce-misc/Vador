repositories {
  mavenCentral()
}
/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

plugins {
  id("io.freefair.lombok")
  id("com.github.spotbugs")
}

dependencies {
  api(libs.hamcrest.core)
  api(libs.hamcrest.date)
  api(libs.java.vavr)
  implementation(libs.kotlin.vavr)
  implementation(libs.typeTools)
  compileOnly(libs.jetbrains.annotations)
  api("de.cronn:reflection-util:2.14.0")

  implementation("org.slf4j:slf4j-api:2.0.0")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.18.0")

  testImplementation(project(":matchers"))
  testImplementation("org.assertj:assertj-vavr:0.4.2")
  testImplementation("org.assertj:assertj-core:3.23.1")
}

if (!System.getProperty("idea.sync.active").toBoolean()) {
  kotlin.sourceSets.main {
    kotlin.setSrcDirs(listOf(tasks.delombok))
  }
}

tasks {
  delombok {
    quiet.set(true)
    input.setFrom("src/main/java")
  }
}
