import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  id("com.adarshr.test-logger")
}
val asciidoclet: Configuration by configurations.creating
val kotestVersion: String by project
dependencies {
  asciidoclet("org.asciidoctor:asciidoclet:1.+")
  testImplementation(platform("org.junit:junit-bom:5.9.0"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testImplementation(platform("io.kotest:kotest-bom:$kotestVersion"))
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}
java {
  withJavadocJar()
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_11
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
  withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
  register("configureJavadoc") {
    doLast {
      javadoc {
        options.doclet = "org.asciidoctor.Asciidoclet"
        options.docletpath = asciidoclet.files.toList()
      }
    }
  }
  javadoc {
    dependsOn("configureJavadoc")
    (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
    isFailOnError = false
    options.encoding("UTF-8")
  }
  withType<PublishToMavenRepository>().configureEach {
    doLast {
      logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to ${repository.name}")
    }
  }
  withType<PublishToMavenLocal>().configureEach {
    doLast {
      logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to MavenLocal.")
    }
  }
}
publishing {
  publications.create<MavenPublication>("vador") {
    val subprojectJarName = tasks.jar.get().archiveBaseName.get()
    artifactId = if (subprojectJarName == "vador") "vador" else "vador-$subprojectJarName"
    from(components["java"])
    pom {
      name.set(artifactId)
      description.set(project.description)
      url.set("https://github.com/salesforce-misc/Vador")
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
        connection.set("scm:git:https://github.com/salesforce-misc/Vador")
        developerConnection.set("scm:git:git@github.com/salesforce-misc/vador.git")
        url.set("https://github.com/salesforce-misc/Vador")
      }
    }
  }
  repositories {
    mavenCentral()
  }
}
