# Contributing

## Source-code Setup

- This is a simple Gradle project and has its own Gradle wrapper. So nothing to install.
- First, You need to add your nexus credentials in `~/.gradle/gradle.properties` file, which are used by your local
  gradle to connect to nexus for downloading artifacts.

  ```properties
  nexusUsername=...
  nexusPassword=...
  ```

- Then run this from project's root:

  ```shell
  ./gradlew clean build
  ```

- For source code navigation you need to have [**Lombok**](https://projectlombok.org/) plugin, which is used to generate
  boilerplate code. There are plugins available for Lombok for all popular IDEs, which you need to install. The latest
  version of the plugin should work.

## Formatting
- This repo uses [**Spotless**](https://github.com/diffplug/spotless) for formatting files with different extensions.
- Please run `./gradlew spotlessApply` before check-in to fix any formatting errors.
- The build fails if there are formatting errors.

--------------------------------------------------------------------------------

## CI/CD

- The CI/CD pipeline is all set up for this library. A CI/CD jenkins job runs for every commit push on any branch.
  However, to publish a jar to nexus, only commits on `master` branch are considered. You can get more information
  from `JenkinsFile` at the root of this repo.
- The job status can be monitored [here](https://ccspaymentsci.dop.sfdc.net/job/validation/job/Vader/job/master/) or
  wait for the build-shield at the top to turn red/green.

## Manual publishing

- If you want to manually publish jar to nexus from you local, you need to have the necessary permissions to release jar
  into Nexus for both SNAPSHOTS and RELEASE channels. This Gus item was raised for Nexus
  access: <https://gus.my.salesforce.com/a07B0000007Qt0BIAS>. Please reach out to this team for any Nexus related
  permissions, problems, or requirements.
- As of today, the permissions are maintained by providing a role `CCSPayments`. We are planning to create an AD group
  for this.
- In `build.gradle.kts` file in root, increment the version property `version = "<Version-Number>"` and run this
  command:

  ```shell
  ./gradlew publish
  ```

- After publishing, you can verify it by searching in the [Nexus Repo](https://nexus.soma.salesforce.com/nexus/index.html#welcome).
