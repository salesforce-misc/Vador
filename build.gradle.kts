import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import kotlinx.kover.api.CoverageEngine
import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  idea
  id("org.jetbrains.kotlinx.kover") version "0.5.0"
  id("io.freefair.lombok") apply false
  id("io.gitlab.arturbosch.detekt") version "1.20.0"
  id("com.adarshr.test-logger") version "3.2.0"
  id("com.diffplug.spotless") version "6.4.2"
  id("org.sonarqube") version "3.3"
  id("org.asciidoctor.jvm.gems") version "3.3.2"
  id("org.asciidoctor.jvm.revealjs") version "3.3.2"
  id("com.github.spotbugs") version "5.0.6"
}

description = "Vader - An FP framework for Bean validation"

// <-- ALL PROJECTS --
allprojects {
  group = "com.salesforce.ccspayments"
  version = "2.7.3-SNAPSHOT"
  apply(plugin = "com.diffplug.spotless")
  spotless {
    kotlin {
      target("src/main/java/**/*.kt", "src/test/java/**/*.kt")
      targetExclude("$buildDir/generated/**/*.*")
      ktlint().userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    }
    kotlinGradle {
      target("*.gradle.kts")
      ktlint().userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    }
    java {
      toggleOffOn()
      target("src/main/java/**/*.java", "src/test/java/**/*.java")
      targetExclude("$buildDir/generated/**/*.*")
      importOrder()
      removeUnusedImports()
      googleJavaFormat()
      trimTrailingWhitespace()
      indentWithSpaces(2)
      endWithNewline()
    }
    format("xml") {
      targetExclude("pom.xml")
      target("*.xml")
      eclipseWtp(XML)
    }
    format("documentation") {
      target("*.md", "*.adoc")
      trimTrailingWhitespace()
      indentWithSpaces(2)
      endWithNewline()
    }
  }
}
// -- ALL PROJECTS -->
// <-- SUB PROJECTS --
subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "maven-publish")
  apply(plugin = "com.adarshr.test-logger")
  apply(plugin = "com.github.spotbugs")

  val asciidoclet: Configuration by configurations.creating
  val lombokForSonarQube: Configuration by configurations.creating

  dependencies {
    asciidoclet("org.asciidoctor:asciidoclet:1.+")
    lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION")

    val testImplementation by configurations
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    val kotestVersion = "5.0.2"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  }
  sonarqube {
    properties {
      property("sonar.projectName", name)
      property("sonar.sources", "src/main")
      property("sonar.tests", "src/test")
      property("sonar.java.libraries", lombokForSonarQube.files.last().toString())
      property("sonar.java.binaries", "build/classes")
    }
  }
  java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
  }
  // <-- SUBPROJECT TASKS --
  tasks {
    withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    spotbugsMain.get().enabled = false
    spotbugsTest.get().enabled = false
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
    withType<KotlinCompile> {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
      }
    }
    test.get().useJUnitPlatform()
    testlogger.theme = MOCHA
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
    spotbugs.ignoreFailures.set(true)
    spotbugsTest.get().enabled = false
  }
  // -- SUBPROJECT TASKS -->
  publishing {
    publications.create<MavenPublication>("mavenJava") {
      val subprojectJarName = tasks.jar.get().archiveBaseName.get()
      artifactId = if (subprojectJarName == "vader") "vader" else "vader-$subprojectJarName"
      from(components["java"])
      pom {
        name.set(artifactId)
        description.set(project.description)
        url.set("https://git.soma.salesforce.com/CCSPayments/Vader")
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
          connection.set("scm:git:https://git.soma.salesforce.com/ccspayments/vader")
          developerConnection.set("scm:git:git@git.soma.salesforce.com:ccspayments/vader.git")
          url.set("https://git.soma.salesforce.com/ccspayments/vader")
        }
      }
    }
    repositories {
      maven {
        name = "Nexus"
        val releasesRepoUrl =
          uri("https://nexus.soma.salesforce.com/nexus/content/repositories/releases")
        val snapshotsRepoUrl =
          uri("https://nexus.soma.salesforce.com/nexus/content/repositories/snapshots")
        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        val nexusUsername: String by project
        val nexusPassword: String by project
        credentials {
          username = nexusUsername
          password = nexusPassword
        }
      }
    }
  }
}
// -- SUB PROJECTS -->
sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property("detekt.sonar.kotlin.config.path", "$rootDir/config/detekt/detekt.yml")
  }
}
kover {
  coverageEngine.set(CoverageEngine.INTELLIJ)
  generateReportOnCheck = true
  runAllTestsForProjectTask = true
}
// <-- ROOT-PROJECT TASKS --
tasks {
  test {
    extensions.configure(KoverTaskExtension::class) {
      isEnabled = true
    }
  }
  koverMergedXmlReport.get().isEnabled = true
  koverMergedHtmlReport.get().isEnabled = false
  koverMergedReport.get().isEnabled = false
  sonarqube {
    properties {
      // As of now this property is ignored until sonarqube is upgraded to 8.9
      // Property from sonar-project.properties is read instead.
      // If that property is not provided, sonar finds it in default path.
      property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "$rootDir/build/reports/kover/report.xml"
      )
      property(
        "sonar.kotlin.detekt.reportPaths",
        "$rootDir/build/reports/detekt/detekt.xml"
      )
    }
  }
  register<Detekt>("detektAll") {
    parallel = true
    ignoreFailures = false
    autoCorrect = false
    buildUponDefaultConfig = true
    basePath = projectDir.toString()
    setSource(file(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.set(File("$rootDir/config/baseline.xml"))
  }
  withType<Detekt>().configureEach {
    reports {
      xml.required.set(true)
    }
  }
}
// -- ROOT-PROJECT TASKS -->
afterEvaluate {
  tasks {
    check.configure {
      dependsOn(named("detektAll"))
    }
    sonarqube.configure { dependsOn(check) }
  }
}
