/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc

plugins {
  id(libs.plugins.kotlin.kapt.get().pluginId)
  id("vador.sub-conventions")
  id("vador.kt-conventions")
  id("vador.publishing-conventions")
}

kotlin { compilerOptions { freeCompilerArgs.add("-Xemit-jvm-type-annotations") } }

dependencies {
  kapt(libs.immutables.value)
  implementation(libs.hamcrest.core)
  implementation(libs.hamcrest.date)
  implementation(libs.java.vavr)
  implementation(libs.kotlin.vavr)
  implementation(libs.typeTools)
  implementation(libs.apache.common.text)
  compileOnly(libs.immutables.value.annotations)
  compileOnly(libs.jetbrains.annotations)
  api(libs.reflection.util)
  implementation(libs.bundles.apache.log4j)
  testImplementation(project(":matchers"))
  testImplementation(libs.assertj.vavr)
  testImplementation(libs.assertj.core)
  testImplementation(libs.bundles.kotest)
}

val generatedKaptMain = layout.buildDirectory.dir("generated/source/kapt/main")

tasks.named<Jar>("sourcesJar") {
  dependsOn("kaptKotlin")
  from(generatedKaptMain)
}

tasks.named<Javadoc>("javadoc") {
  dependsOn("kaptKotlin")
  source(generatedKaptMain)
}
