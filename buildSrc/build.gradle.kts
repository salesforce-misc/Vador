plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}
dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.spotless.gradle)
    implementation(libs.detekt.gradle)
    implementation(libs.kover.gradle)
    implementation(libs.spotbugs.gradle)
    implementation(libs.testLogger.gradle)
}