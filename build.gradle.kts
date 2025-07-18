/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  `java-library`
  id(libs.plugins.detekt.pluginId) apply false
  alias(libs.plugins.lombok.gradle) apply false
  id(libs.plugins.kover.pluginId)
  alias(libs.plugins.nexus.publish)
  id("org.sonarqube") version "6.0.1.5171"
}

allprojects { apply(plugin = "vador.root-conventions") }

kover { reports { total { html { onCheck = true } } } }

dependencies { subprojects.forEach { kover(project(":${it.name}")) } }

val detektReportMerge by
  tasks.registering(ReportMergeTask::class) {
    output = project.layout.buildDirectory.file("reports/detekt/merge.sarif")
  }

subprojects {
  tasks {
    withType<Detekt>().configureEach {
      finalizedBy(detektReportMerge)
      reports { sarif.required = true }
    }
  }
  detektReportMerge { input.from(tasks.withType<Detekt>().map { it.sarifReportFile }) }
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
      rootProject.layout.buildDirectory.file("/build/reports/kover/report.xml"),
    )
    property(
      "detekt.sonar.kotlin.config.path",
      rootProject.layout.buildDirectory.file("/detekt/config.yml"),
    )
    property(
      "sonar.kotlin.detekt.reportPaths",
      rootProject.layout.buildDirectory.file("/build/reports/detekt/merge.xml"),
    )
  }
}

afterEvaluate {
  tasks {
    check.configure { dependsOn(detektReportMerge) }
    sonarqube.configure { dependsOn(check) }
  }
}

nexusPublishing {
  this.repositories {
    sonatype {
      stagingProfileId = STAGING_PROFILE_ID
      nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
      snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
  }
}
