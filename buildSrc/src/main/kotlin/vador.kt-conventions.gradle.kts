import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { kotlin("jvm") }

tasks {
  withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs =
      listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers", "-progressive")
  }
}
