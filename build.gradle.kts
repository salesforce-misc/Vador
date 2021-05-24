import com.adarshr.gradle.testlogger.theme.ThemeType
import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML
import io.freefair.gradle.plugins.lombok.LombokExtension.LOMBOK_VERSION

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
    jacoco
    idea
    id("io.freefair.lombok") version "6.0.0-m2"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    id("com.adarshr.test-logger") version "3.0.0"
    id("com.diffplug.spotless") version "5.12.5"
    id("org.sonarqube") version "3.1.1"
    id("org.barfuin.gradle.taskinfo") version "1.1.1"
}

group = "com.salesforce.ccspayments"
version = "2.4.4-SNAPSHOT"
description = "Vader - An FP framework for Bean validation"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
}

dependencies {
    api("io.vavr:vavr:0.10.3")
    api("io.vavr:vavr-kotlin:0.10.2")
    api("org.hamcrest:hamcrest:2.2")
    api("org.exparity:hamcrest-date:2.0.7")
    api("de.cronn:reflection-util:2.10.0")
    compileOnly("org.jetbrains:annotations:20.1.0")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("com.force.api:swag:0.3.9")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")

    testImplementation(platform("org.junit:junit-bom:5.8.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-vavr:0.4.1")
    testImplementation("org.assertj:assertj-core:3.19.0")
    val kotestVersion = "4.6.0"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.17.0")
}

jacoco.toolVersion = "0.8.7"

if (!providers.systemProperty("idea.sync.active").forUseAtConfigurationTime().orNull.toBoolean()) {
    kotlin.sourceSets.main {
        kotlin.setSrcDirs(listOf(tasks.delombok))
    }
}

val lombokForSonarQube: Configuration by configurations.creating
dependencies {
    lombokForSonarQube("org.projectlombok:lombok:$LOMBOK_VERSION")
}

sonarqube {
    properties {
        property("sonar.sources", lombokForSonarQube.files.last().toString())
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
    test {
        useJUnitPlatform()
    }
    jacocoTestReport {
        reports {
            csv.isEnabled = false
            html.isEnabled = false
            xml.isEnabled = true
        }
    }
}

afterEvaluate {
    tasks {
        check.configure {
            dependsOn(jacocoTestReport)
        }
        jacocoTestReport.configure {
            dependsOn(test)
        }
        named("spotlessKotlin").configure {
            dependsOn(delombok)
        }
    }
}

/********************/
/* Publish to Nexus */
/********************/
tasks {
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
    publications.create<MavenPublication>("mavenJava") {
        artifactId = "vader"
        from(components["java"])
        pom {
            name.set("Vader")
            description.set("An FP framework for Bean validation")
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
    tasks.javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
        // TODO 22/05/21 gopala.akshintala: Turn this on after writing all javadocs
        isFailOnError = false
        options.encoding("UTF-8")
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

detekt {
    baseline = file("${rootProject.projectDir}/config/baseline.xml")
    config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    reports {
        xml {
            enabled = true
        }
        html {
            enabled = false
        }
        txt {
            enabled = false
        }
    }
}

spotless {
    kotlin {
        // by default the target is every '.kt' and '.kts` file in the java sourcesets
        ktlint("0.41.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.41.0")
    }
    java {
        target("src/main/java/**/*.java")
        importOrder()
        removeUnusedImports()
        googleJavaFormat()
    }
    format("xml") {
        targetExclude("pom.xml")
        target("*.xml")
        eclipseWtp(XML)
    }
}
