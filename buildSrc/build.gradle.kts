plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
  implementation("org.jetbrains.kotlinx:kover:0.6.0")
  implementation("com.diffplug.spotless:spotless-plugin-gradle:6.9.1")
  implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.10")
  implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:6.5.0.3")
  
  implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.7.20-Beta")
}
