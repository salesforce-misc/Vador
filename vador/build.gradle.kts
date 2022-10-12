/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.lombok)
}

dependencies {
  api(libs.hamcrest.core)
  api(libs.hamcrest.date)
  api(libs.java.vavr)
  implementation(libs.kotlin.vavr)
  implementation(libs.typeTools)
  compileOnly(libs.jetbrains.annotations)
  api(libs.reflection.util)

  implementation(libs.slf4j.api)
  runtimeOnly(libs.log4j.slf4j18.impl)

  testImplementation(project(":matchers"))
  testImplementation(libs.assertj.vavr)
  testImplementation(libs.assertj.core)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.junit)
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
