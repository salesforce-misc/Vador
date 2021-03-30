plugins {
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "5.3.0"
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.vavr:vavr:0.10.3")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("com.force.api:swag:0.3.9")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")

    testImplementation(platform("org.junit:junit-bom:5.8.0-M1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}

group = "com.salesforce.ccspayments"
version = "2.2.5"
description = "Vader - An FP framework for Bean validation"
java.sourceCompatibility = JavaVersion.VERSION_11

tasks.withType<Test> {
    useJUnitPlatform()
}

/********************/
/* Publish to Nexus */
/********************/
tasks.withType<PublishToMavenRepository>().configureEach {
    doLast {
        logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to ${repository.name}")
    }
}

tasks.withType<PublishToMavenLocal>().configureEach {
    doLast {
        logger.lifecycle("Successfully uploaded ${publication.groupId}:${publication.artifactId}:${publication.version} to MavenLocal.")
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
            val releasesRepoUrl = uri("https://nexus.soma.salesforce.com/nexus/content/repositories/releases")
            val snapshotsRepoUrl = uri("https://nexus.soma.salesforce.com/nexus/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            // nexusUsername, nexusPassword are set in ~/.gradle/gradle.properties by the "GradleInit" method in SFCI
            // refer this for setup: https://git.soma.salesforce.com/MoBE/gradle-init-scripts
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
        isFailOnError = false
        options.encoding("UTF-8")
    }
}
