import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import io.freefair.gradle.plugins.lombok.LombokExtension
import kotlinx.kover.api.DefaultJacocoEngine
import kotlinx.kover.api.KoverTaskExtension
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  id("com.adarshr.test-logger")
}
val kotestVersion: String by project
dependencies {
  testImplementation(platform("org.junit:junit-bom:5.8.2"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testImplementation(platform("io.kotest:kotest-bom:$kotestVersion"))
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
tasks {
  test.get().useJUnitPlatform()
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      freeCompilerArgs = listOf("-Xjdk-release=11")
    }
  }
  testlogger.theme = MOCHA
}
publishing {
  publications.create<MavenPublication>("vader") {
    val subprojectJarName = tasks.jar.get().archiveBaseName.get()
    artifactId = if (subprojectJarName == "vader") "vader" else "vader-$subprojectJarName"
    from(components["java"])
    pom {
      name.set(artifactId)
      description.set(project.description)
      url.set("https://github.com/salesforce/Vader")
      licenses {
        license {
          name.set("The Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }
      developers {
        developer {
          id.set("gopala.akshintala@salesforce.com")
          name.set("Gopal S Akshintala")
          email.set("gopala.akshintala@salesforce.com")
        }
      }
      scm {
        connection.set("scm:git:https://github.com/salesforce/Vader")
        developerConnection.set("scm:git:git@github.com/salesforce/vader.git")
        url.set("https://github.com/salesforce/Vader")
      }
    }
  }
  repositories {
    mavenCentral()
  }
}
