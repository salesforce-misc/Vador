/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  `java-library`
  id(libs.plugins.detekt.pluginId) apply false
  alias(libs.plugins.lombok.gradle) apply false
  id(libs.plugins.kover.pluginId)
  `maven-publish`
  id("io.github.gradle-nexus.publish-plugin")
  signing
  id("org.sonarqube") version "4.4.0.3356"
}

allprojects { apply(plugin = "vador.root-conventions") }

koverReport { defaults { xml { onCheck = true } } }

dependencies { subprojects.forEach { kover(project(":${it.name}")) } }

val detektReportMerge by
  tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
  }

subprojects {
  apply(plugin = "vador.sub-conventions")
  apply(plugin = "vador.kt-conventions")
  apply(plugin = "vador.publishing-conventions")
  tasks.withType<Detekt>().configureEach { reports { xml.required = true } }
  plugins.withType<DetektPlugin> {
    tasks.withType<Detekt> detekt@{
      finalizedBy(detektReportMerge)
      detektReportMerge.configure { input.from(this@detekt.xmlReportFile) }
    }
  }
  val lombokForSonarQube: Configuration by configurations.creating
  dependencies { lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION") }
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

sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      rootProject.layout.buildDirectory.file("/build/reports/kover/report.xml")
    )
    property(
      "detekt.sonar.kotlin.config.path",
      rootProject.layout.buildDirectory.file("/detekt/config.yml")
    )
    property(
      "sonar.kotlin.detekt.reportPaths",
      rootProject.layout.buildDirectory.file("/build/reports/detekt/merge.xml")
    )
  }
}

afterEvaluate {
  tasks {
    check.configure { dependsOn(detektReportMerge) }
    sonarqube.configure { dependsOn(check) }
  }
}

nexusPublishing { this.repositories { sonatype { stagingProfileId = STAGING_PROFILE_ID } } }
