import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import com.diffplug.gradle.spotless.SpotlessTask
import dev.detekt.gradle.Detekt
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
  `java-library`
  id("org.jetbrains.kotlinx.kover")
  id("com.diffplug.spotless")
  id("dev.detekt")
  id("com.adarshr.test-logger")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val jdkVersion = libs.findVersion("jdk").get().requiredVersion.toInt()
val junitVersion = libs.findVersion("junit").get().requiredVersion

java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdkVersion)) } }

tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

tasks.withType<SpotlessTask>().configureEach {
  notCompatibleWithConfigurationCache(
    "Pinned Spotless formatter classloader state fails after configuration-cache restoration.",
  )
}

testing {
  suites { named<JvmTestSuite>("test") { useJUnitJupiter(junitVersion) } }
}

testlogger.theme = MOCHA

spotless {
  lineEndings = PLATFORM_NATIVE
  kotlin {
    target("src/*/kotlin/**/*.kt", "src/*/java/**/*.kt")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("*.gradle.kts", "src/**/*.gradle.kts")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  java {
    target("src/*/java/**/*.java")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    toggleOffOn()
    googleJavaFormat()
    importOrder()
    removeUnusedImports()
    forbidWildcardImports()
    trimTrailingWhitespace()
    leadingSpacesToTabs(2)
    endWithNewline()
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    leadingSpacesToTabs(2)
    endWithNewline()
  }
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  baseline = layout.settingsDirectory.file("detekt/baseline.xml").asFile
  config.setFrom(layout.settingsDirectory.file("detekt/config.yml"))
  ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
  reports {
    checkstyle.required.set(true)
    checkstyle.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
    sarif.required.set(false)
  }
}
