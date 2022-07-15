plugins {
  id("io.freefair.lombok")
  id("com.github.spotbugs")
}

dependencies {
  api(project(":specs"))
  api(libs.hamcrest.core)
  api(libs.hamcrest.date)
  api(libs.java.vavr)
  implementation(libs.kotlin.vavr)
  compileOnly(libs.jetbrains.annotations)
  api("de.cronn:reflection-util:2.13.2")

  implementation("org.slf4j:slf4j-api:2.0.0-alpha1")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.1")

  testImplementation(project(":matchers"))
  testImplementation("org.assertj:assertj-vavr:0.4.2")
  testImplementation("org.assertj:assertj-core:3.22.0")
}

if (!System.getProperty("idea.sync.active").toBoolean()) {
  kotlin.sourceSets.main {
    kotlin.setSrcDirs(listOf(tasks.delombok))
  }
}

tasks {
  delombok {
    quiet.set(true)
    input.setFrom("src/main/java")
  }
}

spotbugs {
  // ! TODO 08/08/21 gopala.akshintala: Probably enable someday
  ignoreFailures.set(true)
}
