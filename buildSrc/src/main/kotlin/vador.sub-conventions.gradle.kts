plugins {
  application
  id("org.jetbrains.kotlinx.kover")
}

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
  withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
  javadoc {
    // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
    isFailOnError = false
    options.encoding("UTF-8")
  }
}
