/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_KOTLIN
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_KOTLIN
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  kotlin("jvm")
  id("org.sonarqube") version "3.4.0.2513"
  id("io.gitlab.arturbosch.detekt") version "1.21.0"
}
allprojects {
  apply(plugin = "vader.root-conventions")
  apply(plugin = "io.gitlab.arturbosch.detekt")
  detekt {
    source = objects.fileCollection().from(
      DEFAULT_SRC_DIR_JAVA,
      DEFAULT_TEST_SRC_DIR_JAVA,
      DEFAULT_SRC_DIR_KOTLIN,
      DEFAULT_TEST_SRC_DIR_KOTLIN
    )
    buildUponDefaultConfig = true
    baseline = file("$rootDir/config/detekt/baseline.xml")
  }
}
val detektReportMerge by tasks.registering(ReportMergeTask::class) {
  output.set(rootProject.buildDir.resolve("reports/detekt/merge.xml"))
}
// <-- SUB PROJECTS --
subprojects {
  apply(plugin = "vader.sub-conventions")
  tasks.withType<Detekt>().configureEach {
    reports {
      xml.required.set(true)
    }
  }
  plugins.withType<DetektPlugin>() {
    tasks.withType<Detekt>() detekt@{
      finalizedBy(detektReportMerge)
      detektReportMerge.configure {
        input.from(this@detekt.xmlReportFile)
      }
    }
  }
  val lombokForSonarQube: Configuration by configurations.creating
  dependencies {
    lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION")
  }
  sonarqube {
    properties {
      property("sonar.projectName", name)
      property("sonar.sources", "src/main")
      property("sonar.tests", "src/test")
      property("sonar.java.libraries", lombokForSonarQube.files.last().toString())
      property("sonar.java.binaries", "build/classes")
    }
  }
}
// -- SUB PROJECTS -->
// <-- ROOT-PROJECT TASKS --
tasks {
  sonarqube {
    properties {
      // As of now this property is ignored until sonarqube is upgraded to 8.9
      // Property from sonar-project.properties is read instead.
      // If that property is not provided, sonar finds it in default path.
      property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "$rootDir/build/reports/kover/report.xml"
      )
      property(
        "sonar.kotlin.detekt.reportPaths",
        "$rootDir/build/reports/detekt/merge.xml"
      )
    }
  }
}
// -- ROOT-PROJECT TASKS -->
sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property("detekt.sonar.kotlin.config.path", "$rootDir/config/detekt/detekt.yml")
  }
}
afterEvaluate {
  tasks {
    check.configure {
      dependsOn(detektReportMerge)
    }
    sonarqube.configure { dependsOn(check) }
  }
}
