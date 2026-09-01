/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
pluginManagement { includeBuild("build-logic") }

plugins { id("com.gradle.develocity") version "4.0" }

dependencyResolutionManagement { repositories { mavenCentral() } }

val isCI = !System.getenv("CI").isNullOrEmpty()

develocity {
  buildScan {
    publishing.onlyIf {
      it.buildResult.failures.isNotEmpty() && !System.getenv("CI").isNullOrEmpty()
    }
    uploadInBackground.set(!isCI)
    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"
  }
}

rootProject.name = "vador-root"

include("matchers")

include("vador")
