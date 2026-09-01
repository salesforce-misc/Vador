import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
  id("vador.jvm-library-conventions")
  kotlin("jvm")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
  jvmToolchain(libs.findVersion("jdk").get().requiredVersion.toInt())
  compilerOptions { freeCompilerArgs.add("-progressive") }
}
