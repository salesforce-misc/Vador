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
  java
  id(libs.plugins.detekt.pluginId) apply false
  alias(libs.plugins.lombok.gradle) apply false
  id(libs.plugins.kover.pluginId)
  `maven-publish`
  id("io.github.gradle-nexus.publish-plugin")
  signing
  id("org.sonarqube") version "4.3.0.3225"
}

allprojects { apply(plugin = "vador.root-conventions") }

koverReport { defaults { xml { onCheck = true } } }

dependencies { subprojects.forEach { kover(project(":${it.name}")) } }

val detektReportMerge by
  tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.buildDir.resolve("reports/detekt/merge.xml"))
  }

subprojects {
  apply(plugin = "vador.sub-conventions")
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
      rootProject.buildDir.resolve("/build/reports/kover/report.xml")
    )
    property("detekt.sonar.kotlin.config.path", rootProject.buildDir.resolve("/detekt/config.yml"))
    property(
      "sonar.kotlin.detekt.reportPaths",
      rootProject.buildDir.resolve("/build/reports/detekt/merge.xml")
    )
  }
}

afterEvaluate {
  tasks {
    check.configure { dependsOn(detektReportMerge) }
    sonarqube.configure { dependsOn(check) }
  }
}

publishing {
  publications.create<MavenPublication>("vador") {
    val subprojectJarName = tasks.jar.get().archiveBaseName.get()
    artifactId = if (subprojectJarName == "vador") "vador" else "vador-$subprojectJarName"
    from(components["java"])
    pom {
      name.set(artifactId)
      description.set(project.description)
      url.set("https://github.com/salesforce-misc/Vador")
      licenses {
        license {
          name.set("The 3-Clause BSD License")
          url.set("https://opensource.org/license/bsd-3-clause/")
        }
      }
      developers {
        developer {
          id.set("overfullstack")
          name.set("Gopal S Akshintala")
          email.set("gopalakshintala@gmail.com")
        }
      }
      scm {
        connection.set("scm:git:https://github.com/salesforce-misc/Vador")
        developerConnection.set("scm:git:git@github.com/salesforce-misc/vador.git")
        url.set("https://github.com/salesforce-misc/Vador")
      }
    }
  }
}

nexusPublishing {
  repositories {
    sonatype {
      username =
        System.getenv("OSSRH_USERNAME")
          ?: project.providers.gradleProperty("ossrhUsername").getOrElse("")
      password =
        System.getenv("OSSRH_PASSWORD")
          ?: project.providers.gradleProperty("ossrhPassword").getOrElse("")
    }
  }
}

signing { sign(publishing.publications["vador"]) }
