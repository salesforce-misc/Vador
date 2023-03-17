import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  signing
  id("com.adarshr.test-logger")
}
repositories {
  mavenCentral()
}
val asciidoclet: Configuration by configurations.creating
dependencies {
  asciidoclet("org.asciidoctor:asciidoclet:1.+")
}
java {
  withJavadocJar()
  withSourcesJar()
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}
testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter("5.9.2")
    }
  }
}
tasks {
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
      logger.lifecycle("Successfully created ${publication.groupId}:${publication.artifactId}:${publication.version} in MavenLocal")
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
  val ossrhUsername: String by lazy { System.getenv("OSSRH_USERNAME") ?: project.providers.gradleProperty("ossrhUsername").get() }
  val ossrhPassword: String by lazy { System.getenv("OSSRH_PASSWORD") ?: project.providers.gradleProperty("ossrhPassword").get() }
  repositories {
    maven {
      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
      url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
      credentials {
        username = ossrhUsername
        password = ossrhPassword
      }
    }
  }
}
signing {
  sign(publishing.publications["vador"])
}
