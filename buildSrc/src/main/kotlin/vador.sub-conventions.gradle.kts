import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.kotlin.dsl.`java-library`

plugins {
  `java-library`
  id("org.jetbrains.kotlinx.kover")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter("5.10.0") }
  }
}
