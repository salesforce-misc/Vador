/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt

plugins {
  kotlin("jvm")
  id("org.sonarqube") version "3.4.0.2513"
  id("io.gitlab.arturbosch.detekt") version "1.21.0"
  id("com.adarshr.test-logger") version "3.2.0"
}
allprojects {
  apply(plugin = "vader.root-conventions")
}
// <-- SUB PROJECTS --
subprojects {
  apply(plugin = "vader.sub-conventions")
  apply(plugin = "com.adarshr.test-logger")

  val asciidoclet: Configuration by configurations.creating
  val lombokForSonarQube: Configuration by configurations.creating
  dependencies {
    asciidoclet("org.asciidoctor:asciidoclet:1.+")
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
  java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
  }
  // <-- SUBPROJECT TASKS --
  tasks {
    withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    register("configureJavadoc") {
      doLast {
        javadoc {
          options.doclet = "org.asciidoctor.Asciidoclet"
          options.docletpath = asciidoclet.files.toList()
        }
      }
    }
    javadoc {
      dependsOn("configureJavadoc")
      (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
      // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
      isFailOnError = false
      options.encoding("UTF-8")
    }
    testlogger.theme = MOCHA
    withType<PublishToMavenRepository>().configureEach {
      doLast {
        logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to ${repository.name}")
      }
    }
    withType<PublishToMavenLocal>().configureEach {
      doLast {
        logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to MavenLocal.")
      }
    }
  }
  // -- SUBPROJECT TASKS -->
}
// -- SUB PROJECTS -->
sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property("detekt.sonar.kotlin.config.path", "$rootDir/config/detekt/detekt.yml")
  }
}
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
        "$rootDir/build/reports/detekt/detekt.xml"
      )
    }
  }
  register<Detekt>("detektAll") {
    parallel = true
    ignoreFailures = false
    autoCorrect = false
    buildUponDefaultConfig = true
    basePath = projectDir.toString()
    setSource(file(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.set(File("$rootDir/config/baseline.xml"))
  }
  withType<Detekt>().configureEach {
    reports {
      xml.required.set(true)
    }
  }
}
// -- ROOT-PROJECT TASKS -->
afterEvaluate {
  tasks {
    check.configure {
      dependsOn(named("detektAll"))
    }
    sonarqube.configure { dependsOn(check) }
  }
}
