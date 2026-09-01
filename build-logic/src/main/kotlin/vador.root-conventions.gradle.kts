import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.qodana.tasks.QodanaScanTask

plugins {
  base
  id("org.jetbrains.kotlinx.kover")
  id("com.diffplug.spotless")
  id("org.sonarqube")
  id("io.github.gradle-nexus.publish-plugin")
  id("org.jetbrains.qodana")
}

val qodanaImage =
  "jetbrains/qodana-jvm-community:2026.2@sha256:8ff36b5cebc0a6d720f77dcf3e0a94a03c39b4c42c3724a99ce5f7e462e42f99"

qodana {
  projectPath.set(layout.projectDirectory.asFile.absolutePath)
  resultsPath.set(layout.buildDirectory.dir("qodana/results").get().asFile.absolutePath)
  cachePath.set(layout.projectDirectory.dir(".qodana/cache").asFile.absolutePath)
}

tasks.named<QodanaScanTask>("qodanaScan") {
  arguments.addAll("--image", qodanaImage)
  dependsOn(":vador:kaptKotlin", ":vador:classes", ":matchers:classes")
}

dependencies {
  kover(project(":matchers"))
  kover(project(":vador"))
}

kover {
  reports {
    filters { excludes { annotatedBy("org.immutables.value.Generated") } }
    total {
      html { onCheck = true }
      xml { onCheck = true }
    }
  }
}

spotless {
  lineEndings = PLATFORM_NATIVE
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
}

val detektReportMerge: TaskProvider<ReportMergeTask> =
  tasks.register<ReportMergeTask>("detektReportMerge") {
    output.set(layout.buildDirectory.file("reports/detekt/merge.xml"))
    input.from(
      layout.projectDirectory.file("matchers/build/reports/detekt/detekt.xml"),
      layout.projectDirectory.file("vador/build/reports/detekt/detekt.xml"),
    )
    dependsOn(":matchers:detekt", ":vador:detekt")
  }

tasks.named("check") { dependsOn(detektReportMerge) }

sonarqube {
  properties {
    property("sonar.projectName", rootProject.name)
    property("sonar.sources", "matchers/src/main,vador/src/main")
    property("sonar.tests", "matchers/src/test,vador/src/test")
    property("sonar.java.binaries", "matchers/build/classes,vador/build/classes")
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      layout.buildDirectory.file("reports/kover/report.xml").get().asFile.absolutePath,
    )
    property(
      "detekt.sonar.kotlin.config.path",
      layout.projectDirectory.file("detekt/config.yml").asFile.absolutePath,
    )
    property(
      "sonar.kotlin.detekt.reportPaths",
      layout.buildDirectory.file("reports/detekt/merge.xml").get().asFile.absolutePath,
    )
  }
}

tasks.named("sonarqube") { dependsOn(tasks.named("check")) }

nexusPublishing {
  repositories {
    sonatype {
      stagingProfileId.set(providers.gradleProperty("vador.stagingProfileId"))
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
}
