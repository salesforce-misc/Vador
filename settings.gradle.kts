/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
plugins { id("com.gradle.enterprise") version "3.13" }

dependencyResolutionManagement {
  versionCatalogs { create("libs") { from(files("libs.versions.toml")) } }
}

gradleEnterprise {
  if (System.getenv("CI") != null) {
    buildScan {
      publishAlways()
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
    }
  }
}

rootProject.name = "vador-root"

include("matchers")

include("vador")
