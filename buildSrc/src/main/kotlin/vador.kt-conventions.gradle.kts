import org.gradle.kotlin.dsl.kotlin

plugins {
  kotlin("jvm")
  kotlin("plugin.lombok")
}

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies { testImplementation(libs.kotestBundle) }

kotlin {
  jvmToolchain(libs.jdk.toString().toInt())
  compilerOptions { freeCompilerArgs.addAll("-progressive", "-Xmulti-dollar-interpolation") } }

kotlinLombok {
  lombokConfigurationFile(file("../lombok.config"))
}
