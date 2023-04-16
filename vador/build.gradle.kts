/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

plugins {
  alias(libs.plugins.lombok.gradle)
}

dependencies {
  implementation(libs.hamcrest.core)
  implementation(libs.hamcrest.date)
  implementation(libs.java.vavr)
  implementation(libs.kotlin.vavr)
  implementation(libs.typeTools)
  compileOnly(libs.jetbrains.annotations)
  api(libs.reflection.util)
  implementation(libs.bundles.apache.log4j)
  testImplementation(project(":matchers"))
  testImplementation(libs.assertj.vavr)
  testImplementation(libs.assertj.core)
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
