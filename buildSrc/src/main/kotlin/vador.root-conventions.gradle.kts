import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE

plugins {
  java
  idea
  id("com.diffplug.spotless")
  id("io.gitlab.arturbosch.detekt")
  id("com.adarshr.test-logger")
  id("com.github.spotbugs") apply false
}

repositories { mavenCentral() }

spotless {
  lineEndings = PLATFORM_NATIVE
  kotlin {
    ktfmt().googleStyle()
    target("src/*/kotlin/**/*.kt", "src/*/java/**/*.kt")
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("build/**", ".gradle/**", "generated/**", "bin/**", "out/**", "tmp/**")
  }
  kotlinGradle {
    ktfmt().googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
    targetExclude("build/**", ".gradle/**", "generated/**", "bin/**", "out/**", "tmp/**")
  }
  java {
    toggleOffOn()
    target("src/*/java/**/*.java")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
    targetExclude("build/**", ".gradle/**", "generated/**", "bin/**", "out/**", "tmp/**")
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
}

testlogger.theme = MOCHA

tasks {
  spotbugsMain.get().enabled = false
  spotbugsTest.get().enabled = false
  spotbugs.ignoreFailures.set(true)
}
