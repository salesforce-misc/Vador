plugins {
  `maven-publish`
  jacoco
}

dependencies {
  api(libs.hamcrest.core)
  api(libs.hamcrest.date)
  api(libs.kotlin.vavr)
  api(libs.java.vavr)
}

publishing {
  publications.create<MavenPublication>("mavenJava") {
    artifactId = "vader-matchers"
    from(components["java"])
    pom {
      name.set("Vader Matchers")
      description.set("Matchers to be used for Vader Specs")
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
