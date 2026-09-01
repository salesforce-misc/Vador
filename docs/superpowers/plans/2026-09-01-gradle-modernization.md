# Gradle 9 and Java 25 Modernization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate Vador to an included `build-logic` build, Gradle 9.7.1, Kotlin 2.4.20-RC2, and Java 25 while preserving its modules and published artifacts and adding accurate `AGENTS.md` guidance.

**Architecture:** Settings own build composition and dependency repositories; a standalone included build exposes focused JVM-library, Kotlin-library, publishing, and root convention plugins. Module scripts retain only module-specific plugins, compiler options, and dependencies, while the root convention owns aggregate reports, Sonar inputs, and Nexus configuration.

**Tech Stack:** Gradle 9.7.1 Kotlin DSL, Kotlin/JVM 2.4.20-RC2, Java 25 toolchains, KAPT/Immutables, Kover 0.9.9, Detekt 2.0.0-alpha.6, Spotless 8.10.1, SonarScanner for Gradle, Gradle Nexus Publish Plugin, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-01-gradle-modernization-design.md`

## Global Constraints

- Use Gradle 9.7.1 with binary distribution checksum `acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a` and wrapper JAR checksum `7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d`.
- Use Java 25 for the Gradle daemon, Java compilation, Kotlin compilation, tests, KAPT, and Javadoc; emitted JVM class major version must be 69.
- Upgrade Kotlin from 2.1.10 to 2.4.20-RC2 because 2.1.10 does not support Gradle 9 or Java 25 targeting. Reproduced failures authorize only Kover 0.9.9, Spotless 8.10.1, and Detekt 2.0.0-alpha.6; preserve every other dependency and plugin version.
- Preserve projects `:vador` and `:matchers`, Maven coordinates `com.salesforce.vador:vador:1.1.1-SNAPSHOT` and `com.salesforce.vador:vador-matchers:1.1.1-SNAPSHOT`, publication name `vador`, dependency roles, source layout, generated sources, POM metadata, signing, and Sonatype endpoints.
- Keep configuration-cache problems fail-closed. A third-party execution task may opt out only through its typed task with a precise reason; never restore global warning mode.
- Do not enable isolated projects or parallel configuration-cache storage in this change.
- Do not publish to Maven Central, write to Maven Local, upload a Sonar analysis, modify generated KAPT sources, or alter application/test source unless a Java 25 build-owned incompatibility is reproduced.
- Use `./gradlew`, never a system Gradle installation. Keep unrelated user changes untouched.

## Baseline Evidence

- Build-configuration baseline revision: `1a23448` (design plus Kotlin compatibility correction;
  implementation begins after this plan is committed).
- `./gradlew --version`: Gradle 8.14.1, launcher/daemon Java 21.0.11.
- `./gradlew clean build --warning-mode all --configuration-cache-problems=fail --console=plain`: exit 0, 69 actionable tasks, configuration-cache entry stored.
- Test baseline: 159 tests across 26 XML suites, zero failures, zero errors, zero skipped.
- Existing artifacts: `vador-1.1.1-SNAPSHOT.jar`, `vador-1.1.1-SNAPSHOT-sources.jar`, `vador-1.1.1-SNAPSHOT-javadoc.jar`, and the corresponding `matchers-*` files.
- Existing aggregate coverage is HTML-only at `build/reports/kover/html/index.html`; Detekt produces `build/reports/detekt/merge.sarif`; the Sonar paths point elsewhere and are therefore stale.

## File Responsibility Map

**Create**

- `.gitattributes` — deterministic LF/CRLF policy for generated wrapper scripts.
- `.sdkmanrc` — exact local Java 25 SDKMAN environment.
- `AGENTS.md` — repository structure, commands, style, generated-source rules, and safety guidance.
- `build-logic/settings.gradle.kts` — included-build plugin and dependency resolution.
- `build-logic/build.gradle.kts` — convention-plugin implementation classpath.
- `build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts` — Java, tests, formatting, analysis, and coverage policy shared by modules.
- `build-logic/src/main/kotlin/vador.kotlin-library-conventions.gradle.kts` — Kotlin JVM toolchain and compiler policy.
- `build-logic/src/main/kotlin/vador.root-conventions.gradle.kts` — aggregate reports, root formatting, Sonar, and Nexus configuration.
- `build-logic/src/main/kotlin/vador.publishing-conventions.gradle.kts` — publication metadata and generated-source artifacts.
- `gradle/gradle-daemon-jvm.properties` — generated Java 25 daemon criteria.
- `docs/superpowers/plans/2026-09-01-gradle-modernization.md` — this execution plan.

**Move**

- `buildSrc/build.gradle.kts` to `build-logic/build.gradle.kts`.
- `buildSrc/settings.gradle.kts` to `build-logic/settings.gradle.kts`.
- `buildSrc/src/main/kotlin/**` to `build-logic/src/main/kotlin/**` before replacing the old conventions.
- `libs.versions.toml` to `gradle/libs.versions.toml`.

**Modify**

- `settings.gradle.kts` — include `build-logic`, auto-import the catalog, centralize repositories, and add the Java toolchain resolver.
- `build.gradle.kts` — reduce the root to `vador.root-conventions`.
- `gradle.properties` — namespaced publication values and strict cache policy.
- `matchers/build.gradle.kts` — apply layered module conventions.
- `vador/build.gradle.kts` — apply layered conventions while retaining KAPT and its one compiler option.
- `.github/workflows/build.yml` — Java 25 and `gradle/actions/setup-gradle@v4`.
- `CONTRIBUTING.adoc` — Java 25, SDKMAN environment, build-logic release-value location, and wrapper-only commands.
- `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat` — generated Gradle 9.7.1 wrapper.

**Delete**

- `buildSrc/**` after the tracked move.
- `build-logic/src/main/kotlin/Config.kt` and `predef.kt` after replacing compiled constants/helpers.
- `build-logic/src/main/kotlin/vador.kt-conventions.gradle.kts` and `vador.sub-conventions.gradle.kts` after module migration.
- `sonar-project.properties` after the root convention becomes the sole Sonar source.

---

### Task 1: Establish the included-build, catalog, and provider foundations

**Files:**

- Move: `buildSrc/**` → `build-logic/**`
- Move: `libs.versions.toml` → `gradle/libs.versions.toml`
- Modify: `settings.gradle.kts`
- Modify: `build.gradle.kts`
- Modify: `gradle.properties`
- Modify: `build-logic/settings.gradle.kts`
- Modify: `build-logic/src/main/kotlin/vador.publishing-conventions.gradle.kts`
- Delete: `build-logic/src/main/kotlin/Config.kt`
- Delete: `build-logic/src/main/kotlin/predef.kt`

**Interfaces:**

- Consumes: the existing four convention IDs and existing Gradle 8.14.1 behavior.
- Produces: `pluginManagement { includeBuild("build-logic") }`, the auto-imported root `libs` catalog, explicit included-build catalog import, and Gradle properties `vador.group`, `vador.version`, and `vador.stagingProfileId`.

- [ ] **Step 1: Run the structural assertion before the move**

Run:

```bash
test -d build-logic && test ! -e buildSrc && test -f gradle/libs.versions.toml && test ! -e libs.versions.toml
```

Expected: FAIL because `buildSrc` and the root catalog still exist.

- [ ] **Step 2: Move the tracked build and catalog**

Run:

```bash
git mv buildSrc build-logic
git mv libs.versions.toml gradle/libs.versions.toml
```

Expected: Git records path moves without losing convention sources or catalog history.

- [ ] **Step 3: Register and configure the included build**

Make the beginning of `settings.gradle.kts` follow this shape, retaining the existing copyright,
Develocity block, root name, and includes:

```kotlin
pluginManagement { includeBuild("build-logic") }

plugins { id("com.gradle.develocity") version "4.0" }

dependencyResolutionManagement { repositories { mavenCentral() } }
```

Replace `build-logic/settings.gradle.kts` with:

```kotlin
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

rootProject.name = "build-logic"
```

- [ ] **Step 4: Move release values to providers**

Append these exact values to `gradle.properties`:

```properties
vador.group=com.salesforce.vador
vador.version=1.1.1-SNAPSHOT
vador.stagingProfileId=1ea0a23e61ba7d
```

In `vador.publishing-conventions.gradle.kts`, replace compiled constants with:

```kotlin
val groupId = providers.gradleProperty("vador.group")
val releaseVersion = providers.gradleProperty("vador.version")

group = groupId.get()
version = releaseVersion.get()
```

In the root Nexus configuration, replace `STAGING_PROFILE_ID` with:

```kotlin
stagingProfileId = providers.gradleProperty("vador.stagingProfileId").get()
```

- [ ] **Step 5: Remove root dependence on `buildSrc` helpers**

Change the root plugin declarations from helper-based `id(...)` calls to catalog aliases:

```kotlin
plugins {
  `java-library`
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.kover)
  alias(libs.plugins.nexus.publish)
  id("org.sonarqube") version "6.0.1.5171"
}
```

Delete `build-logic/src/main/kotlin/Config.kt` and
`build-logic/src/main/kotlin/predef.kt`; verify no symbol remains:

```bash
rg -n "GROUP_ID|STAGING_PROFILE_ID|\bVERSION\b|pluginId|kotestBundle|\.jdk\b" build.gradle.kts settings.gradle.kts build-logic --glob '!**/build/**'
```

Expected: no matches in build scripts or build-logic sources.

- [ ] **Step 6: Build the included build independently**

Run:

```bash
./gradlew -p build-logic build --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: PASS; all four legacy convention plugins compile from the included build.

- [ ] **Step 7: Verify the root build remains green on Gradle 8**

Run:

```bash
./gradlew clean build --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: PASS with `:vador` and `:matchers` tests, publications, KAPT sources, and reports unchanged.

- [ ] **Step 8: Re-run the structural assertion**

Run:

```bash
test -d build-logic && test ! -e buildSrc && test -f gradle/libs.versions.toml && test ! -e libs.versions.toml
```

Expected: PASS.

- [ ] **Step 9: Commit the included-build foundation**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/libs.versions.toml build-logic
git add -u buildSrc libs.versions.toml
git diff --cached --check
git commit -m "build: migrate conventions to included build"
```

---

### Task 2: Introduce layered module and publishing conventions

**Files:**

- Create: `build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts`
- Create: `build-logic/src/main/kotlin/vador.kotlin-library-conventions.gradle.kts`
- Modify: `build-logic/src/main/kotlin/vador.publishing-conventions.gradle.kts`
- Modify: `matchers/build.gradle.kts`
- Modify: `vador/build.gradle.kts`
- Delete: `build-logic/src/main/kotlin/vador.kt-conventions.gradle.kts`
- Delete: `build-logic/src/main/kotlin/vador.sub-conventions.gradle.kts`

**Interfaces:**

- Consumes: catalog versions `jdk`, `junit`, Kotlin, Kover, Spotless, Detekt, and test logger.
- Produces: internal plugin IDs `vador.jvm-library-conventions`, `vador.kotlin-library-conventions`, and the preserved `vador.publishing-conventions`; module XML Detekt reports at `<module>/build/reports/detekt/detekt.xml`.

- [ ] **Step 1: Run the new-plugin structural assertion**

Run:

```bash
test -f build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts && test -f build-logic/src/main/kotlin/vador.kotlin-library-conventions.gradle.kts
```

Expected: FAIL because the layered plugins do not exist.

- [ ] **Step 2: Create the JVM-library convention**

Create `vador.jvm-library-conventions.gradle.kts` with this responsibility-complete shape:

```kotlin
import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import dev.detekt.gradle.Detekt
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jvmsuite.JvmTestSuite

plugins {
  `java-library`
  id("org.jetbrains.kotlinx.kover")
  id("com.diffplug.spotless")
  id("dev.detekt")
  id("com.adarshr.test-logger")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val jdkVersion = libs.findVersion("jdk").get().requiredVersion.toInt()
val junitVersion = libs.findVersion("junit").get().requiredVersion

java { toolchain { languageVersion.set(JavaLanguageVersion.of(jdkVersion)) } }

tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }

testing {
  suites { named<JvmTestSuite>("test") { useJUnitJupiter(junitVersion) } }
}

testlogger.theme = MOCHA

spotless {
  lineEndings = PLATFORM_NATIVE
  kotlin {
    target("src/*/kotlin/**/*.kt", "src/*/java/**/*.kt")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlinGradle {
    target("*.gradle.kts", "src/**/*.gradle.kts")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  java {
    target("src/*/java/**/*.java")
    targetExclude("build/**", ".gradle/**", "generated/**", "**/bin/**", "out/**", "tmp/**")
    toggleOffOn()
    googleJavaFormat()
    importOrder()
    removeUnusedImports()
    forbidWildcardImports()
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
}

detekt {
  parallel = true
  buildUponDefaultConfig = true
  baseline = layout.settingsDirectory.file("detekt/baseline.xml").asFile
  config.setFrom(layout.settingsDirectory.file("detekt/config.yml"))
  ignoreFailures = true
}

tasks.withType<Detekt>().configureEach {
  reports {
    checkstyle.required.set(true)
    checkstyle.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
    sarif.required.set(false)
  }
}
```

- [ ] **Step 3: Create the Kotlin-library convention**

Create `vador.kotlin-library-conventions.gradle.kts`:

```kotlin
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
  id("vador.jvm-library-conventions")
  kotlin("jvm")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
  jvmToolchain(libs.findVersion("jdk").get().requiredVersion.toInt())
  compilerOptions { freeCompilerArgs.add("-progressive") }
}
```

Do not carry forward `-Xcontext-receivers`; repository search has confirmed there are no context
receiver declarations.

- [ ] **Step 4: Make publishing own KAPT-generated artifacts lazily**

Keep the existing POM metadata, signing, Javadoc policy, and artifact mapping. Remove the repository
and Java-toolchain blocks from `vador.publishing-conventions.gradle.kts`, since settings and the JVM
convention now own them. Replace eager task reads with project-name mapping:

```kotlin
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
```

Move the generated-source behavior out of `vador/build.gradle.kts` and guard it by KAPT plugin
presence in the publishing convention:

```kotlin
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
```

- [ ] **Step 5: Apply the new module plugins**

Change `matchers/build.gradle.kts` to:

```kotlin
plugins {
  id("vador.kotlin-library-conventions")
  id("vador.publishing-conventions")
}
```

Change the plugin block in `vador/build.gradle.kts` to:

```kotlin
plugins {
  alias(libs.plugins.kotlin.kapt)
  id("vador.kotlin-library-conventions")
  id("vador.publishing-conventions")
}
```

Keep `-Xemit-jvm-type-annotations` and all dependency declarations. Delete the module-local
`sourcesJar` and `javadoc` blocks now owned by publishing convention.

- [ ] **Step 6: Delete the superseded conventions and compile build logic**

Delete:

```text
build-logic/src/main/kotlin/vador.kt-conventions.gradle.kts
build-logic/src/main/kotlin/vador.sub-conventions.gradle.kts
```

Run:

```bash
./gradlew -p build-logic clean build --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: PASS with the new JVM, Kotlin, and publishing plugin descriptors.

- [ ] **Step 7: Verify both modules and generated sources**

Run:

```bash
./gradlew :matchers:clean :matchers:build :vador:clean :vador:build --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: PASS. Verify `vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar` still includes both
handwritten Kotlin and generated Java:

```bash
jar tf vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar | rg 'com/salesforce/vador/config/IDConfig\.(kt|java)'
```

Expected: both `IDConfig.kt` and `IDConfig.java`.

- [ ] **Step 8: Commit layered module conventions**

```bash
git add build-logic/src/main/kotlin matchers/build.gradle.kts vador/build.gradle.kts
git diff --cached --check
git commit -m "build: layer JVM library conventions"
```

---

### Task 3: Centralize root reporting, Sonar, repositories, and Nexus

**Files:**

- Modify: `gradle/libs.versions.toml`
- Modify: `build-logic/build.gradle.kts`
- Replace: `build-logic/src/main/kotlin/vador.root-conventions.gradle.kts`
- Replace: `build.gradle.kts`
- Modify: `settings.gradle.kts`
- Delete: `sonar-project.properties`

**Interfaces:**

- Consumes: module Detekt XML files, module Kover data, `vador.stagingProfileId`, and the fixed module paths `:vador` and `:matchers`.
- Produces: `build/reports/kover/html/index.html`, `build/reports/kover/report.xml`, `build/reports/detekt/merge.xml`, root `check` aggregation, and local inputs for `sonarqube`.

- [ ] **Step 1: Show that required Sonar reports are absent**

Run:

```bash
test -s build/reports/kover/report.xml && test -s build/reports/detekt/merge.xml
```

Expected: FAIL because the baseline produces HTML coverage and merged SARIF only.

- [ ] **Step 2: Put root plugin implementations on the included-build classpath**

Add the pinned Sonar version and plugin-marker libraries to `gradle/libs.versions.toml`:

```toml
[versions]
sonarqube = "6.0.1.5171"

[libraries]
nexus-publish-gradle = { module = "io.github.gradle-nexus.publish-plugin:io.github.gradle-nexus.publish-plugin.gradle.plugin", version.ref = "nexus-publish" }
sonarqube-gradle = { module = "org.sonarqube:org.sonarqube.gradle.plugin", version.ref = "sonarqube" }
```

Add to `build-logic/build.gradle.kts`:

```kotlin
implementation(libs.nexus.publish.gradle)
implementation(libs.sonarqube.gradle)
```

Remove the `spotbugs` version, library, plugin alias, and build-logic dependency. Remove unused
JUnit BOM/API/engine libraries and the unused `junit` bundle; retain the `junit = "5.12.0"` version
consumed by the JVM test suite.

- [ ] **Step 3: Replace the root convention with explicit aggregation**

Replace `vador.root-conventions.gradle.kts` with this shape:

```kotlin
import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import dev.detekt.gradle.report.ReportMergeTask
import org.gradle.api.tasks.TaskProvider

plugins {
  base
  id("org.jetbrains.kotlinx.kover")
  id("com.diffplug.spotless")
  id("org.sonarqube")
  id("io.github.gradle-nexus.publish-plugin")
}

dependencies {
  kover(project(":matchers"))
  kover(project(":vador"))
}

kover {
  reports {
    filters { excludes { annotatedBy("org.immutables.value.Generated") } }
    total {
      html { onCheck = true }
      xml { onCheck = true }
    }
  }
}

spotless {
  lineEndings = PLATFORM_NATIVE
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt("0.53").googleStyle()
    trimTrailingWhitespace()
    endWithNewline()
  }
  format("documentation") {
    target("*.md", "*.adoc")
    trimTrailingWhitespace()
    leadingTabsToSpaces(2)
    endWithNewline()
  }
}

val detektReportMerge: TaskProvider<ReportMergeTask> =
  tasks.register<ReportMergeTask>("detektReportMerge") {
    output.set(layout.buildDirectory.file("reports/detekt/merge.xml"))
    input.from(
      layout.projectDirectory.file("matchers/build/reports/detekt/detekt.xml"),
      layout.projectDirectory.file("vador/build/reports/detekt/detekt.xml"),
    )
    dependsOn(":matchers:detekt", ":vador:detekt")
  }

tasks.named("check") { dependsOn(detektReportMerge) }

sonarqube {
  properties {
    property("sonar.projectName", rootProject.name)
    property("sonar.sources", "matchers/src/main,vador/src/main")
    property("sonar.tests", "matchers/src/test,vador/src/test")
    property("sonar.java.binaries", "matchers/build/classes,vador/build/classes")
    property(
      "sonar.coverage.jacoco.xmlReportPaths",
      layout.buildDirectory.file("reports/kover/report.xml").get().asFile.absolutePath,
    )
    property(
      "detekt.sonar.kotlin.config.path",
      layout.projectDirectory.file("detekt/config.yml").asFile.absolutePath,
    )
    property(
      "sonar.kotlin.detekt.reportPaths",
      layout.buildDirectory.file("reports/detekt/merge.xml").get().asFile.absolutePath,
    )
  }
}

tasks.named("sonarqube") { dependsOn(tasks.named("check")) }

nexusPublishing {
  repositories {
    sonatype {
      stagingProfileId.set(providers.gradleProperty("vador.stagingProfileId"))
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
}
```

- [ ] **Step 4: Reduce the root build and enforce repository ownership**

Replace the non-copyright content of `build.gradle.kts` with:

```kotlin
plugins { id("vador.root-conventions") }
```

Change root dependency resolution in `settings.gradle.kts` to:

```kotlin
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories { mavenCentral() }
}
```

Delete `sonar-project.properties` so there is one Sonar source of truth.

- [ ] **Step 5: Compile the root convention and inspect the task graph**

Run:

```bash
./gradlew -p build-logic clean build --warning-mode all --configuration-cache-problems=fail --console=plain
./gradlew check --dry-run --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: both PASS. The dry run includes `:matchers:detekt`, `:vador:detekt`,
`detektReportMerge`, `koverXmlReport`, and `koverHtmlReport` without a root Java/SpotBugs test
lifecycle.

- [ ] **Step 6: Generate and validate the report inputs**

Run:

```bash
./gradlew clean check --warning-mode all --configuration-cache-problems=fail --console=plain
test -s build/reports/kover/report.xml
test -s build/reports/kover/html/index.html
test -s build/reports/detekt/merge.xml
rg -n "sonar.coverage.jacoco.xmlReportPaths|sonar.kotlin.detekt.reportPaths" build-logic/src/main/kotlin/vador.root-conventions.gradle.kts
```

Expected: all commands PASS; configured Sonar paths name the files just generated.

- [ ] **Step 7: Confirm obsolete cross-project and SpotBugs configuration is gone**

Run:

```bash
git grep -n -E 'allprojects|subprojects|afterEvaluate|spotbugs|sonar\.modules|vador\.(kt|sub)-conventions' -- ':!docs/superpowers/**'
```

Expected: no matches.

- [ ] **Step 8: Commit root convention ownership**

```bash
git add build.gradle.kts settings.gradle.kts gradle/libs.versions.toml build-logic sonar-project.properties
git diff --cached --check
git commit -m "build: centralize root quality conventions"
```

---

### Task 4: Upgrade Gradle, Kotlin, and the Java runtime

**Files:**

- Create: `.gitattributes`
- Create: `.sdkmanrc`
- Create: `gradle/gradle-daemon-jvm.properties` through `updateDaemonJvm`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Modify: `gradle.properties`
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Modify: `gradle/wrapper/gradle-wrapper.jar`
- Modify: `gradlew`
- Modify: `gradlew.bat`
- Modify: `.github/workflows/build.yml`
- Modify: `build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts`
- Modify: `build-logic/src/main/kotlin/vador.root-conventions.gradle.kts`
- Modify: `detekt/config.yml`
- Modify: `vador/src/main/kotlin/com/salesforce/vador/execution/strategies/FailFastStrategies.kt`
- Modify: `docs/superpowers/specs/2026-09-01-gradle-modernization-design.md`
- Modify: `docs/superpowers/plans/2026-09-01-gradle-modernization.md`

**Interfaces:**

- Consumes: the Gradle-8-compatible included build from Tasks 1–3.
- Produces: Gradle 9.7.1 wrapper with verified checksums, Kotlin 2.4.20-RC2, Java 25 daemon/toolchains, JVM class major 69, and Java 25 CI.

- [ ] **Step 1: Run the runtime postcondition before upgrading**

Run:

```bash
./gradlew --version | rg 'Gradle 9\.7\.1|Launcher JVM:  25|Daemon JVM:.*25'
```

Expected: FAIL; the baseline is Gradle 8.14.1 on Java 21.

- [ ] **Step 2: Set the compatible Kotlin and Java versions**

Change these catalog values:

```toml
jdk = "25"
kotlin = "2.4.20-RC2"
kover = "0.9.9"
detekt = "2.0.0-alpha.6"
spotless = "8.10.1"
```

Compatibility upgrades are limited to three reproduced failures. Kover 0.9.1 requested the
removed `compileKotlinTask` property, so it moves to stable 0.9.9. The resumed Java 25 build showed
Spotless 7.0.2 failing in ktfmt/Google Java Format Java-compiler integration, so it moves to stable
8.10.1 with Java 25 formatter support. Detekt 1.23.8 rejected JVM target 25 because its supported
targets end at 22; no stable 1.x target-25 line exists, so it moves to the JDK-25-tested
2.0.0-alpha.6 used by the reference stack. Keep every other dependency and plugin version
unchanged.

Migrate Detekt's catalog marker to `dev.detekt:detekt-gradle-plugin`, its alias/plugin ID to
`dev.detekt`, and convention imports to `dev.detekt.gradle`. Configure per-module
`checkstyle.required` and `checkstyle.outputLocation` at the existing
`build/reports/detekt/detekt.xml` path, preserve SARIF disabled, and use
`dev.detekt.gradle.report.ReportMergeTask` with `output.set(...)` for the unchanged root merged XML
and Sonar path. Remove the obsolete Detekt 1 `build.maxIssues` config block while preserving the
active comments and line-length rules. Remove the Spotless 7 custom wildcard-import formatter and
use Spotless 8's native `forbidWildcardImports()` without changing Java/document tab formatting.
Because Spotless 8 changes unversioned ktfmt from 0.53 to 0.63, use
`ktfmt("0.53").googleStyle()` in every JVM/root Kotlin and Kotlin-Gradle format to preserve the
Spotless 7 output without editing application or test sources.

Kotlin 2.4.20-RC2 reproduces `UNSAFE_CALL` in
`failFastForEachBatchOfBatch1` because its explicit `fold<Either<...>?>` result is nullable before
the `mapLeft` call. Remove only the single outer `?` from that fold result type; retain the nullable
payload type arguments, both branches, the adjacent behavior, and `-progressive`. Verify the
focused compile and `BatchOfBatch1ValidationConfigTest` before the strict module build.

Replace the cache migration escape hatch in `gradle.properties`:

```properties
org.gradle.configuration-cache.problems=fail
```

Keep build cache, configuration cache, parallel execution, KAPT K2, Sonar skip-compile, and Kotlin
build-report properties unchanged.

- [ ] **Step 3: Add deterministic Java provisioning inputs**

Create `.sdkmanrc`:

```properties
# Enable auto-env through the sdkman_auto_env config
java=25.0.4-jbr
```

Add the resolver settings plugin alongside Develocity:

```kotlin
plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.0"
}
```

- [ ] **Step 4: Generate the verified Gradle 9.7.1 wrapper twice**

Run once with Gradle 8 to update the distribution, then again through the new distribution to
refresh the scripts and wrapper JAR:

```bash
./gradlew :wrapper --gradle-version 9.7.1 --distribution-type bin --gradle-distribution-sha256-sum acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a --network-timeout 120000
./gradlew :wrapper --gradle-version 9.7.1 --distribution-type bin --gradle-distribution-sha256-sum acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a --network-timeout 120000 --retries 0 --retry-back-off-ms 500
```

- [ ] **Step 5: Generate Java 25 daemon criteria**

Run with `JAVA_HOME` and `PATH` selecting the Foojay-provisioned Java 25 home:

```bash
./gradlew updateDaemonJvm --jvm-version 25
```

Expected: `gradle/gradle-daemon-jvm.properties` is generated with `toolchainVersion=25` and Foojay
download URLs. Do not hand-edit generated platform URLs.

- [ ] **Step 6: Update CI to the supported runtime and setup action**

Change `.github/workflows/build.yml` to:

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: 25

- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4

- name: Execute Gradle build
  run: ./gradlew build --warning-mode all
```

- [ ] **Step 7: Verify wrapper integrity and runtime selection**

Run with `JAVA_HOME` and `PATH` selecting the Foojay-provisioned Java 25 home:

```bash
rg -n 'gradle-9\.7\.1-bin\.zip|distributionSha256Sum=acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a|networkTimeout=120000' gradle/wrapper/gradle-wrapper.properties
shasum -a 256 gradle/wrapper/gradle-wrapper.jar
./gradlew --version
```

Expected wrapper JAR hash:

```text
7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d
```

Expected runtime: Gradle 9.7.1 with launcher and daemon Java 25.

- [ ] **Step 8: Compile and verify Java 25 bytecode**

Run with the same Java 25 `JAVA_HOME` and `PATH`:

```bash
./gradlew clean :matchers:build :vador:build --warning-mode all --configuration-cache-problems=fail --console=plain
javap -verbose -classpath vador/build/libs/vador-1.1.1-SNAPSHOT.jar com.salesforce.vador.execution.Vador | rg 'major version: 69'
javap -verbose -classpath matchers/build/libs/matchers-1.1.1-SNAPSHOT.jar com.salesforce.vador.matchers.AnyMatchers | rg 'major version: 69'
```

Expected: build PASS and both `javap` checks PASS. If a pinned non-Kotlin plugin emits an explicit
Gradle 9 compatibility failure, stop with the exact stack trace and amend the spec/plan before
changing its version.

Track the generated wrapper scripts with explicit platform line endings:

```gitattributes
gradlew text eol=lf
gradlew.bat text eol=crlf
```

Stage the generated scripts through this policy; do not hand-edit their generated content.

- [ ] **Step 9: Commit the runtime upgrade**

```bash
git add -f .sdkmanrc
git add .gitattributes .github/workflows/build.yml settings.gradle.kts gradle.properties gradle/libs.versions.toml gradle/gradle-daemon-jvm.properties gradle/wrapper build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts build-logic/src/main/kotlin/vador.root-conventions.gradle.kts detekt/config.yml vador/src/main/kotlin/com/salesforce/vador/execution/strategies/FailFastStrategies.kt docs/superpowers/specs/2026-09-01-gradle-modernization-design.md docs/superpowers/plans/2026-09-01-gradle-modernization.md
git add --renormalize gradlew gradlew.bat
git diff --cached --check
git commit -m "build: upgrade to Gradle 9 and Java 25"
```

---

### Task 5: Add repository-specific agent and contributor guidance

**Files:**

- Create: `AGENTS.md`
- Modify: `CONTRIBUTING.adoc`

**Interfaces:**

- Consumes: final convention names, Java 25, generated-source behavior, and verification commands.
- Produces: self-contained agent guidance and human contributor instructions that do not refer to retired `buildSrc` paths or Java 17.

- [ ] **Step 1: Prove the guidance is missing or stale**

Run:

```bash
test -f AGENTS.md && ! rg -n 'Java 17|buildSrc|gradle-build-action@v3' AGENTS.md CONTRIBUTING.adoc
```

Expected: FAIL because `AGENTS.md` does not exist and `CONTRIBUTING.adoc` names Java 17/buildSrc.

- [ ] **Step 2: Create `AGENTS.md` with Vador-specific guidance**

Create this structure and content:

````markdown
# Vador – Functional Validation for the JVM

Kotlin/Gradle library that publishes a validation DSL and Java-friendly matcher artifacts.
Requires JDK 25; always use the checked-in Gradle wrapper.

## Project Structure

- `vador/src/main/kotlin/` - Core validation DSL and execution strategies
- `vador/src/test/` - Kotlin and Java API/compatibility tests
- `matchers/src/main/` - Reusable Vador, Hamcrest, and Vavr matchers
- `matchers/src/test/` - Matcher tests
- `build-logic/` - Precompiled Gradle convention plugins
- `gradle/libs.versions.toml` - Dependency, plugin, Kotlin, and JDK versions
- `detekt/` - Static-analysis configuration and baseline

## Development

```bash
./gradlew build
./gradlew :vador:test
./gradlew :matchers:test
./gradlew :vador:test --tests "com.salesforce.vador.compatibility.JavaDslCompatibilityTest"
./gradlew spotlessApply
./gradlew koverHtmlReport koverXmlReport
```

Run `./gradlew clean build --warning-mode all --console=plain` before handing off a change.

## Style

- Follow the existing functional Kotlin style: immutable values and transformations over mutable state.
- Prefer expression-oriented Kotlin and collection combinators where they keep intent clear.
- Preserve Java-callable public APIs and the Java compatibility tests when changing the Kotlin DSL.
- Let Spotless/ktfmt and Google Java Format own formatting; do not hand-format around them.
- Keep dependency and plugin versions in `gradle/libs.versions.toml`.
- Keep shared build policy in the focused convention under `build-logic`; module scripts own only module-specific configuration.

## Generated Sources

- Immutables Java sources under `vador/build/generated/source/kapt/` are generated outputs.
- Never edit or commit generated sources.
- Changes to KAPT inputs must preserve generated sources in the published sources JAR and Javadoc inputs.

## Gradle and Publishing

- Configuration-cache problems fail the build. Use a typed task-level incompatibility declaration only for a reproduced third-party limitation.
- Validate publications with `generatePomFileForVadorPublication` and `generateMetadataFileForVadorPublication` tasks.
- Do not run Maven Central publishing, signing, Maven Local publication, or Sonar upload tasks without explicit authorization and the required credentials.
````

- [ ] **Step 3: Update contributor runtime and release-value guidance**

In `CONTRIBUTING.adoc`:

- rename `Install Java 17` to `Install Java 25`;
- replace the version-specific install example with `sdk env install` and explain that `.sdkmanrc`
  pins `25.0.4-jbr`;
- keep `./gradlew clean build` and change the IDE formatting text from `gradle spotlessApply` to
  `./gradlew spotlessApply`;
- replace the `buildSrc/.../Config.kt` release-version link with `gradle.properties` and name the
  `vador.version` property;
- retain the current explicit publishing command and credential guidance.

Use this Java setup block:

```adoc
=== Install Java 25

Vador requires JDK 25. The repository's `.sdkmanrc` pins the development runtime.
After installing https://sdkman.io/install[SDKMAN], run:

[source,bash]
----
sdk env install
----
```

- [ ] **Step 4: Format and verify documentation**

Run:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck --warning-mode all --configuration-cache-problems=fail --console=plain
test -f AGENTS.md
! rg -n 'Java 17|buildSrc|gradle-build-action@v3' AGENTS.md CONTRIBUTING.adoc
git diff --check
```

Expected: every command PASS.

- [ ] **Step 5: Commit repository guidance**

```bash
git add AGENTS.md CONTRIBUTING.adoc
git diff --cached --check
git commit -m "docs: add Vador agent guidance"
```

---

### Task 6: Qualify the migrated build and publications

**Files:**

- Verify: all changed build, wrapper, CI, and documentation files
- Modify only if reproduced: `build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts` for a typed Spotless cache opt-out

**Interfaces:**

- Consumes: all deliverables from Tasks 1–5.
- Produces: strict-cache model reuse, complete Java 25 build evidence, report evidence, publication metadata, artifact-content evidence, and a clean final status.

- [ ] **Step 1: Verify model storage and reuse**

Run twice:

```bash
./gradlew help --warning-mode all --configuration-cache-problems=fail --console=plain
./gradlew help --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: first run stores or reuses a valid entry; second run reports `Configuration cache entry reused.`

- [ ] **Step 2: Run the complete clean build**

Run:

```bash
./gradlew clean build --warning-mode all --configuration-cache-problems=fail --console=plain
```

Expected: PASS. If and only if it fails after cache serialization with
`Could not initialize class com.facebook.ktfmt.format.Parser` or missing formatter runtime classes,
add this narrow exception to the JVM convention:

```kotlin
import com.diffplug.gradle.spotless.SpotlessTask

tasks.withType<SpotlessTask>().configureEach {
  notCompatibleWithConfigurationCache(
    "Pinned Spotless formatter classloader state fails after configuration-cache restoration.",
  )
}
```

Then rerun the clean build. Do not change formatter/toolchain versions or global cache policy for
that failure.

- [ ] **Step 3: Confirm the behavioral test baseline**

Run:

```bash
find matchers/build/test-results vador/build/test-results -name 'TEST-*.xml' -print0 | xargs -0 awk -F'"' '/<testsuite / { tests += $4; skipped += $6; failures += $8; errors += $10 } END { printf "tests=%d failures=%d errors=%d skipped=%d\n", tests, failures, errors, skipped }'
```

Expected:

```text
tests=159 failures=0 errors=0 skipped=0
```

- [ ] **Step 4: Verify Sonar input reports**

Run:

```bash
test -s build/reports/kover/report.xml
test -s build/reports/kover/html/index.html
test -s build/reports/detekt/merge.xml
rg -n '<report|<package' build/reports/kover/report.xml
rg -n '<checkstyle|<file' build/reports/detekt/merge.xml
```

Expected: every command PASS. Do not execute `sonarqube` against a live endpoint.

- [ ] **Step 5: Generate and inspect publication metadata without publishing**

Run:

```bash
./gradlew :matchers:generatePomFileForVadorPublication :matchers:generateMetadataFileForVadorPublication :vador:generatePomFileForVadorPublication :vador:generateMetadataFileForVadorPublication --warning-mode all --configuration-cache-problems=fail --console=plain
rg -n '<groupId>com\.salesforce\.vador</groupId>|<artifactId>vador-matchers</artifactId>|<version>1\.1\.1-SNAPSHOT</version>' matchers/build/publications/vador/pom-default.xml
rg -n '<groupId>com\.salesforce\.vador</groupId>|<artifactId>vador</artifactId>|<version>1\.1\.1-SNAPSHOT</version>' vador/build/publications/vador/pom-default.xml
test -s matchers/build/publications/vador/module.json
test -s vador/build/publications/vador/module.json
```

Expected: tasks PASS and both coordinate checks find all three values.

- [ ] **Step 6: Inspect artifacts and Java 25 bytecode**

Run:

```bash
test -s matchers/build/libs/matchers-1.1.1-SNAPSHOT.jar
test -s matchers/build/libs/matchers-1.1.1-SNAPSHOT-sources.jar
test -s matchers/build/libs/matchers-1.1.1-SNAPSHOT-javadoc.jar
test -s vador/build/libs/vador-1.1.1-SNAPSHOT.jar
test -s vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar
test -s vador/build/libs/vador-1.1.1-SNAPSHOT-javadoc.jar
jar tf vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar | rg 'com/salesforce/vador/config/IDConfig\.(kt|java)'
jar tf matchers/build/libs/matchers-1.1.1-SNAPSHOT-sources.jar | rg 'com/salesforce/vador/matchers/DateMatchers\.kt'
jar tf matchers/build/libs/matchers-1.1.1-SNAPSHOT-javadoc.jar | rg '^META-INF/MANIFEST\.MF$'
jar tf vador/build/libs/vador-1.1.1-SNAPSHOT-javadoc.jar | rg 'com/salesforce/vador/config/IDConfig\.html'
javap -verbose -classpath vador/build/libs/vador-1.1.1-SNAPSHOT.jar com.salesforce.vador.execution.Vador | rg 'major version: 69'
```

Expected: all six artifacts exist; the sources JAR contains handwritten and generated forms; JVM
major version is 69. The matchers sources JAR contains Kotlin matcher sources and its preserved
manifest-only Javadoc JAR contains `META-INF/MANIFEST.MF`; the Vador Javadoc JAR contains the
generated `IDConfig.html` page.

- [ ] **Step 7: Run final structural and Git checks**

Run:

```bash
test -d build-logic
test ! -e buildSrc
test -f gradle/libs.versions.toml
test ! -e libs.versions.toml
git grep -n -E 'allprojects|subprojects|afterEvaluate|spotbugs|sonar\.modules|vador\.(kt|sub)-conventions' -- ':!docs/superpowers/**'
git diff --check
git status --short --branch
```

Expected: structural assertions PASS, repository search has no output, diff check passes, and Git
status is clean. If the typed Spotless exception was necessary, commit only that reproduced fix:

```bash
git add build-logic/src/main/kotlin/vador.jvm-library-conventions.gradle.kts
git diff --cached --check
git commit -m "build: isolate Spotless from configuration cache"
```

- [ ] **Step 8: Summarize qualification evidence**

Record in the handoff:

```text
Gradle: 9.7.1
Java daemon/toolchain/bytecode: 25 / 25 / major 69
Kotlin: 2.4.20-RC2
Tests: 159 passed, 0 failed, 0 errored, 0 skipped
Configuration-cache model reuse: confirmed
Reports: Kover HTML + XML, merged Detekt XML confirmed
Publications: vador and vador-matchers POM/module metadata confirmed
External gates omitted: Maven Central publication and live Sonar upload
Final Git status: clean
```
