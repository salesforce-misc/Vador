import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlinx.kover")
}

repositories { mavenCentral() }

val asciidoclet: Configuration by configurations.creating

dependencies { asciidoclet("org.asciidoctor:asciidoclet:1.+") }

java {
  withJavadocJar()
  withSourcesJar()
  toolchain { languageVersion.set(JavaLanguageVersion.of(11)) }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter("5.10.0") }
  }
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      freeCompilerArgs = listOf("-Xjdk-release=11")
    }
  }
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
}
