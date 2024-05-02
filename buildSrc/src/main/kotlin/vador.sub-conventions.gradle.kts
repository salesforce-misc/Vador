import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.`java-library`

plugins {
  `java-library`
  id("org.jetbrains.kotlinx.kover")
  id("io.gitlab.arturbosch.detekt")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter("5.10.2") }
  }
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  baseline = file("$rootDir/detekt/baseline.xml")
  config.setFrom(file("$rootDir/detekt/config.yml"))
  ignoreFailures = true
}
