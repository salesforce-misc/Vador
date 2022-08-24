import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import kotlinx.kover.api.DefaultJacocoEngine
import kotlinx.kover.api.KoverTaskExtension

plugins {
  java
  idea
  id("com.diffplug.spotless")
  id("org.jetbrains.kotlinx.kover")
  id("io.freefair.lombok") apply false
  id("com.github.spotbugs") apply false
}

version = "3.3.8-SNAPSHOT"
group = "com.salesforce.ccspayments"
description = "Vader - A framework for POJO/Data Structure/Bean validation"

repositories {
  mavenCentral()
}
kover {
  isDisabled.set(false)
  engine.set(DefaultJacocoEngine)
}
koverMerged {
  enable()
  xmlReport {
    onCheck.set(true)
  }
  htmlReport {
    onCheck.set(true)
  }
}
spotless {
  kotlin {
    target("src/main/java/**/*.kt", "src/test/java/**/*.kt")
    targetExclude("$buildDir/generated/**/*.*")
    ktlint()
      .setUseExperimental(true)
      .editorConfigOverride(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktlint()
      .setUseExperimental(true)
      .editorConfigOverride(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
  }
  java {
    toggleOffOn()
    target("src/main/java/**/*.java", "src/test/java/**/*.java")
    targetExclude("$buildDir/generated/**/*.*")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
  format("xml") {
    targetExclude("pom.xml")
    target("*.xml")
    eclipseWtp(XML)
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
}
tasks {
  spotbugsMain.get().enabled = false
  spotbugsTest.get().enabled = false
  spotbugs.ignoreFailures.set(true)
  test {
    extensions.configure(KoverTaskExtension::class) {
      isEnabled = true
    }
  }
}
