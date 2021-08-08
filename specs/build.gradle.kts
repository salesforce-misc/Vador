plugins {
  id("io.freefair.lombok")
  id("com.github.spotbugs")
}

dependencies {
  api(libs.hamcrest.core)
  api(libs.java.vavr)
  compileOnly(libs.jetbrains.annotations)
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
