import org.gradle.kotlin.dsl.kotlin

plugins { kotlin("jvm") }

kotlin { compilerOptions { freeCompilerArgs.addAll("-Xcontext-receivers", "-progressive") } }
