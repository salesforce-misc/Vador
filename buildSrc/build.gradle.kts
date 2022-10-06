plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
val kotlinVersion: String by project
dependencies {
  implementation("org.jetbrains.kotlinx:kover:0.6.1")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.11.0")
  implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.10")
  implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:6.5.0.3")
  
  implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
  implementation("com.adarshr.test-logger:com.adarshr.test-logger.gradle.plugin:3.2.0")
}
