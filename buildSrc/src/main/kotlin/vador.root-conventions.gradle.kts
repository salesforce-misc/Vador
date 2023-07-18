import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.LineEnding
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_SRC_DIR_KOTLIN
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_JAVA
import io.gitlab.arturbosch.detekt.extensions.DetektExtension.Companion.DEFAULT_TEST_SRC_DIR_KOTLIN

plugins {
  java
  idea
  id("com.diffplug.spotless")
  id("io.gitlab.arturbosch.detekt")
  id("com.adarshr.test-logger")
  id("com.github.spotbugs") apply false
}

version = "1.0.2-SNAPSHOT"
group = "com.salesforce.vador"
description = "Vador - A framework for POJO/Data Structure/Bean validation"
repositories {
  mavenCentral()
}
spotless {
  lineEndings = LineEnding.PLATFORM_NATIVE
  kotlin {
    ktfmt().googleStyle()
    target("**/*.kt")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**")
  }
  kotlinGradle {
    ktfmt().googleStyle()
    target("**/*.gradle.kts")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**")
  }
  java {
    toggleOffOn()
    target("**/*.java")
    targetExclude("$buildDir/generated/**/*.*")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
    targetExclude("**/build/**", "**/.gradle/**", "**/generated/**")
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
detekt {
  source = objects.fileCollection().from(
    DEFAULT_SRC_DIR_JAVA,
    DEFAULT_TEST_SRC_DIR_JAVA,
    DEFAULT_SRC_DIR_KOTLIN,
    DEFAULT_TEST_SRC_DIR_KOTLIN
  )
  parallel = true
  buildUponDefaultConfig = true
  baseline = file("$rootDir/detekt/baseline.xml")
  config = files("$rootDir/detekt/detekt.yml")
}
testlogger.theme = MOCHA
tasks {
  spotbugsMain.get().enabled = false
  spotbugsTest.get().enabled = false
  spotbugs.ignoreFailures.set(true)
}
