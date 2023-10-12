/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
dependencies {
  implementation(libs.hamcrest.core)
  implementation(libs.hamcrest.date)
  api(libs.kotlin.vavr)
  api(libs.java.vavr)
  testImplementation(libs.bundles.kotest)
}
