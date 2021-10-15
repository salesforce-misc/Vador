import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  `maven-publish`
  idea
  jacoco
  id("io.freefair.lombok")
  id("io.gitlab.arturbosch.detekt") version "1.18.0"
  id("com.adarshr.test-logger") version "3.0.0"
  id("com.diffplug.spotless") version "5.15.2"
  id("org.sonarqube") version "3.3"
  id("org.asciidoctor.jvm.gems") version "3.3.2"
  id("org.asciidoctor.jvm.revealjs") version "3.3.2"
  id("com.github.spotbugs") version "4.7.2"
}

allprojects {
  group = "com.salesforce.ccspayments"
  version = "2.7.1"
  repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
  }
  apply(plugin = "com.diffplug.spotless")
  spotless {
    kotlin {
      target("src/main/java/**/*.kt", "src/test/java/**/*.kt")
      targetExclude("$buildDir/generated/**/*.*")
      ktlint("0.42.1").userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    }
    kotlinGradle {
      target("*.gradle.kts")
      ktlint("0.42.1").userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
    }
    java {
      target("src/main/java/**/*.java", "src/test/java/**/*.java")
      targetExclude("$buildDir/generated/**/*.*")
      importOrder()
      removeUnusedImports()
      googleJavaFormat("1.11.0")
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

description = "Vader - An FP framework for Bean validation"

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "jacoco")
  apply(plugin = "com.adarshr.test-logger")
  apply(plugin = "com.github.spotbugs")

  val asciidoclet: Configuration by configurations.creating
  val lombokForSonarQube: Configuration by configurations.creating

  dependencies {
    asciidoclet("org.asciidoctor:asciidoclet:1.+")
    lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION")

    val testImplementation by configurations
    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    val kotestVersion = "4.6.1"
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
      property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "$rootDir/build/reports/jacoco/test/jacocoTestReport.xml"
      )
      property(
        "sonar.kotlin.detekt.reportPaths",
        "$rootDir/build/reports/detekt/detekt.xml"
      )
    }
  }
  java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
  }
  tasks {
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
      if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
      }
      // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
      isFailOnError = false
      options.encoding("UTF-8")
    }
    withType<KotlinCompile> {
      kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
      }
    }
    test.get().useJUnitPlatform()
    jacocoTestReport {
      reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
      }
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
    testlogger {
      theme = ThemeType.MOCHA
      showExceptions = true
      showStackTraces = true
      showFullStackTraces = true
      showCauses = true
      slowThreshold = 2000
      showSummary = true
      showSimpleNames = true
      showPassed = true
      showSkipped = true
      showFailed = true
      showStandardStreams = true
      showPassedStandardStreams = true
      showSkippedStandardStreams = true
      showFailedStandardStreams = true
      logLevel = LogLevel.LIFECYCLE
    }
    spotbugs.ignoreFailures.set(true)
    spotbugsTest.get().enabled = false
  }
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
sonarqube {
  properties {
    property("sonar.modules", subprojects.joinToString(",") { it.name })
    property("detekt.sonar.kotlin.config.path", "$rootDir/config/detekt/detekt.yml")
  }
}
tasks {
  jacocoTestReport {
    dependsOn(subprojects.map { it.tasks.withType<Test>() })
    dependsOn(subprojects.map { it.tasks.withType<JacocoReport>() })
    sourceDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.setFrom(subprojects.map { it.the<SourceSetContainer>()["main"].output })
    executionData.setFrom(
      project.fileTree(".") {
        include("**/build/jacoco/test.exec")
      }
    )
    reports {
      xml.required.set(true)
      csv.required.set(false)
      html.required.set(false)
    }
  }
  register<Detekt>("detektAll") {
    parallel = true
    ignoreFailures = false
    autoCorrect = false
    buildUponDefaultConfig = true
    basePath = projectDir.toString()
    setSource(subprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline.set(File("$rootDir/config/baseline.xml"))
    reports {
      xml.enabled = true
      html.enabled = false
      txt.enabled = false
    }
  }
}
afterEvaluate {
  tasks {
    check.configure {
      dependsOn(jacocoTestReport)
      dependsOn(named("detektAll"))
    }
    sonarqube.configure { dependsOn(check) }
  }
}
