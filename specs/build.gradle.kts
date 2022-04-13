plugins {
  id("io.freefair.lombok")
}

dependencies {
  api(libs.hamcrest.core)
  api(libs.java.vavr)
  compileOnly(libs.jetbrains.annotations)
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
