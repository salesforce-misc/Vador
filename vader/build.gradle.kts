plugins {
  id("io.freefair.lombok")
  id("com.github.spotbugs")
}

dependencies {
  api(project(":specs"))
  api(libs.hamcrest.core)
  api(libs.hamcrest.date)
  implementation(libs.kotlin.vavr)
  api(libs.java.vavr)
  compileOnly(libs.jetbrains.annotations)
  api("de.cronn:reflection-util:2.10.0")

  implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
  implementation("com.force.api:swag:0.3.9")

  runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")


  testImplementation(project(":matchers"))
  testImplementation("org.assertj:assertj-vavr:0.4.1")
  testImplementation("org.assertj:assertj-core:3.19.0")
}

if (!providers.systemProperty("idea.sync.active").forUseAtConfigurationTime().orNull.toBoolean()) {
  kotlin.sourceSets.main {
    kotlin.setSrcDirs(listOf(tasks.delombok))
  }
  sourceSets.main {
    java.setSrcDirs(listOf(tasks.delombok))
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
