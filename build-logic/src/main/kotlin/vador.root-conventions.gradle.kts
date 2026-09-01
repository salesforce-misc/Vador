import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  base
  id("org.jetbrains.kotlinx.kover")
  id("com.diffplug.spotless")
  id("org.sonarqube")
  id("io.github.gradle-nexus.publish-plugin")
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
    ktfmt().googleStyle()
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

val detektReportMerge by tasks.registering(ReportMergeTask::class) {
  output = layout.buildDirectory.file("reports/detekt/merge.xml")
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
