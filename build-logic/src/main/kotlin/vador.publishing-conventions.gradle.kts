import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.withType

plugins {
  `maven-publish`
  signing
  `java-library`
}

val groupId = providers.gradleProperty("vador.group")
val releaseVersion = providers.gradleProperty("vador.version")

group = groupId.get()
version = releaseVersion.get()

description = "Vador - A framework for POJO/Data Structure/Bean validation"

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications.create<MavenPublication>("vador") {
    artifactId = if (project.name == "vador") "vador" else "vador-${project.name}"
    from(components["java"])
    pom {
      name.set(artifactId)
      description.set(project.description)
      url.set("https://github.com/salesforce-misc/Vador")
      inceptionYear.set("2020")
      licenses {
        license {
          name.set("The Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
      }
      developers {
        developer {
          id.set("overfullstack")
          name.set("Gopal S Akshintala")
          email.set("gopalakshintala@gmail.com")
        }
      }
      scm {
        connection.set("scm:git:https://github.com/salesforce-misc/Vador")
        developerConnection.set("scm:git:git@github.com/salesforce-misc/vador.git")
        url.set("https://github.com/salesforce-misc/Vador")
      }
    }
  }
}

signing { sign(publishing.publications["vador"]) }

pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
  val generatedKaptMain = layout.buildDirectory.dir("generated/source/kapt/main")
  tasks.named<Jar>("sourcesJar") {
    dependsOn("kaptKotlin")
    from(generatedKaptMain)
  }
  tasks.named<Javadoc>("javadoc") {
    dependsOn("kaptKotlin")
    source(generatedKaptMain)
  }
}

tasks {
  withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
  javadoc {
    // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
    isFailOnError = false
    options.encoding("UTF-8")
  }
}
