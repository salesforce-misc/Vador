# Gradle 9 and Java 25 Modernization Design

## Objective

Modernize Vador's Gradle build around Gradle 9.7.1 and Java 25, replace the legacy
`buildSrc` build with Gradle's recommended included `build-logic` build, correct known
configuration drift, and add repository-specific agent guidance. Preserve the library's
modules, public artifacts, source layout, build behavior, and pinned dependency and plugin
versions except for the Kotlin compatibility upgrade and any other narrow upgrade demonstrated
necessary by Gradle 9.7.1 or Java 25.

## Current State

Vador is a two-module Kotlin/JVM library build:

- `vador` publishes the core `com.salesforce.vador:vador` artifact;
- `matchers` publishes `com.salesforce.vador:vador-matchers`;
- shared build policy is implemented by four precompiled Kotlin DSL plugins in `buildSrc`;
- the version catalog is stored at the repository root and imported manually;
- the wrapper uses Gradle 8.14.1;
- the publishing convention and contributor guide require Java 17, while the version catalog
  and GitHub Actions still declare Java 11;
- configuration-cache problems are permanently reduced to warnings;
- root configuration reaches into every project through `allprojects`, `subprojects`, and
  `afterEvaluate` blocks;
- the JUnit version is duplicated and inconsistent;
- SpotBugs is present but all of its tasks are disabled;
- the Sonar coverage and Detekt paths do not match the reports that the build produces.

The existing Gradle 8.14.1 model passes `help` and a `build --dry-run` with configuration-cache
problems set to fail. The migration is therefore a behavior-preserving modernization rather than
recovery from a broken configuration model.

## Non-Goals

This work will not:

- change application or test source code except where Java 25 compilation reveals a build-owned
  compatibility problem;
- rename `:vador` or `:matchers`;
- change Maven coordinates, publication contents, or signing semantics;
- broadly update Kotlin, libraries, Gradle plugins, or GitHub Actions unrelated to the build;
- enable incubating isolated-project or parallel configuration-cache flags;
- publish to Maven Central or upload a live Sonar analysis;
- edit generated KAPT sources.

## Build Architecture

### Settings and dependency resolution

`settings.gradle.kts` will register `build-logic` from `pluginManagement` so its convention
plugins are available to project plugin blocks. It will retain the Develocity settings plugin and
the existing failure-only CI publication policy for build scans.

The version catalog will move from `/libs.versions.toml` to the conventional
`/gradle/libs.versions.toml` location, allowing the root build to import `libs` automatically.
The included build will explicitly import `../gradle/libs.versions.toml`, because included builds
do not inherit the root catalog.

Root dependency repositories will be centralized in settings and guarded by
`RepositoriesMode.FAIL_ON_PROJECT_REPOS`. `build-logic` will retain its own plugin-resolution and
implementation-dependency repositories because it is an independent build. No convention plugin
will add repositories to consumer projects.

### Included build

The tracked `buildSrc` directory will be replaced by a standalone `build-logic` included build.
It will use the `kotlin-dsl` plugin and depend only on the Gradle plugin implementations needed by
its convention plugins. Shared helpers may remain ordinary Kotlin source inside `build-logic`, but
project coordinates and release values will not be compiled constants.

The included build will expose four precompiled convention plugins:

#### `vador.jvm-library-conventions`

This plugin owns policy common to both published modules:

- `java-library` and a Java 25 toolchain;
- JUnit Platform test-suite configuration using the catalog's JUnit 5.12.0 version;
- Kover instrumentation;
- Spotless configuration for Kotlin, Java, Kotlin Gradle scripts, and documentation;
- Detekt configuration using the repository's baseline and configuration files;
- the existing test-logger presentation;
- lazy task configuration through `configureEach` and named providers.

SpotBugs will be removed from the catalog and build logic because every existing SpotBugs task is
disabled. Removing a permanently disabled tool does not change the build's enforced quality gates.

#### `vador.kotlin-library-conventions`

This plugin composes `vador.jvm-library-conventions` with the Kotlin JVM plugin. It owns the Java
25 Kotlin toolchain and preserves progressive compilation. The unused `-Xcontext-receivers` flag
will be removed because no production or test source declares a context receiver. Module-specific
compiler arguments remain in the owning module.

#### `vador.publishing-conventions`

This plugin owns Maven publication, signing, artifact naming, sources and Javadoc JARs, generated
KAPT sources, duplicate handling, and POM metadata. It preserves publication names and maps the
`:vador` project to artifact `vador` and `:matchers` to artifact `vador-matchers` without eagerly
reading task state.

Group, version, and Sonatype staging-profile values will move from `Config.kt` constants to
namespaced Gradle properties. Convention logic will access them through providers so changing a
release value does not recompile the included build.

#### `vador.root-conventions`

This plugin applies only to the root project and owns:

- aggregate Kover coverage for the explicit `:vador` and `:matchers` modules;
- HTML coverage for developers and a JaCoCo-compatible XML report for Sonar;
- module Detekt report production and a root XML merge consumed by Sonar;
- root lifecycle dependencies using lazy task providers;
- Sonar source, test, binary, coverage, and Detekt paths for the two known modules;
- Nexus publishing repository configuration.

The root plugin will not iterate over or configure subproject task objects. Aggregate tasks will
depend on explicit module task paths and consume explicitly located report files. The obsolete
`sonar.modules` configuration and duplicated `sonar-project.properties` file will be removed so
the Gradle convention is the single Sonar configuration source.

### Consumer build scripts

The root `build.gradle.kts` will apply only `vador.root-conventions`.

Both module build scripts will apply `vador.kotlin-library-conventions` and
`vador.publishing-conventions`. `vador` will continue applying KAPT and its module-specific
`-Xemit-jvm-type-annotations` option. Dependency declarations remain module-local and retain their
current versions and API-versus-implementation roles.

The existing vague `vador.kt-conventions` and `vador.sub-conventions` identifiers will be retired.
No `allprojects`, `subprojects`, or `afterEvaluate` configuration will remain.

## Gradle and Java Runtime Policy

The complete wrapper will be regenerated for Gradle 9.7.1, including Unix and Windows scripts,
the wrapper JAR, wrapper properties, and the official binary distribution checksum. Repository
attributes will keep the generated `gradlew` script LF and `gradlew.bat` CRLF so Git preserves
their platform line endings without rejecting regenerated content as whitespace errors.

Java 25 will be the single runtime and compilation baseline:

- `jdk = "25"` in the version catalog;
- Java and Kotlin toolchains set from that catalog entry;
- Gradle daemon JVM criteria requiring Java 25;
- `.sdkmanrc` aligned with ReVoman's `25.0.4-jbr` development runtime;
- GitHub Actions using Java 25;
- contributor and agent documentation stating Java 25.

The build will continue enabling the build cache, configuration cache, and parallel task
execution. Configuration-cache problems will use fail mode. Warning mode will not be retained as a
permanent escape hatch. Incubating isolated-project and parallel configuration-cache modes are not
part of this migration.

GitHub Actions will replace the retired `gradle/gradle-build-action@v3` setup with
`gradle/actions/setup-gradle@v4`, then execute the wrapper build as before.

## Compatibility Changes

Gradle, Java, Kotlin, the wrapper checksum, the Gradle setup action, and the repository structure
are intentional upgrades. Kotlin will move from 2.1.10 to 2.4.20-RC2: Kotlin 2.1.10 does not
support Gradle 9 or Java 25 targeting, while 2.4.20-RC2 is the current compatible release candidate
and matches ReVoman's verified Gradle 9.7.1/Java 25 stack. Dependency and other Gradle plugin
versions otherwise remain pinned.

If a pinned Gradle plugin cannot configure or execute on Gradle 9.7.1 or cannot compile for the
Java 25 target, implementation may upgrade only that plugin to the minimum stable compatible
version. Such an upgrade must be supported by a reproducible Gradle 9 or Java 25 failure,
documented in the implementation record, and revalidated through the complete build. Speculative
or broad dependency updates are out of scope.

Task 4 reproduced that exception for Kover 0.9.1 under Kotlin 2.4.20-RC2 and Gradle 9.7.1:
`:matchers:koverGenerateArtifactJvm` failed because Kover requested the removed
`compileKotlinTask` compilation property. The authorized compatibility correction upgrades only
Kover from 0.9.1 to the current stable 0.9.9 recommended by its official documentation; that
release line includes Gradle 9 fixes.

The resumed Java 25 build then reproduced two further bounded plugin failures. Spotless 7.0.2's
ktfmt and Google Java Format integrations fail against Java 25 compiler APIs, so Spotless is
authorized to move to stable 8.10.1, which supplies Java 25 formatter support and the native
`forbidWildcardImports()` step. Detekt 1.23.8 rejects JVM target 25 because it supports targets only
through 22; no stable 1.x line supports target 25, so Detekt is authorized to move to
2.0.0-alpha.6, the ReVoman-compatible line built with Kotlin 2.4.10 and Gradle 9.6.1 and tested on
JDK 25. Detekt's plugin ID/packages and XML report name migrate to `dev.detekt` and Checkstyle,
while the existing module output paths, merged XML, SARIF-disabled policy, and Sonar path remain
unchanged. The removed Detekt 1 `build.maxIssues` configuration key is dropped; the active comments
and line-length policy remains unchanged. Spotless 8 changes its unversioned ktfmt default from
0.53 to 0.63, which produces application-source formatting drift; every Kotlin and Kotlin-Gradle
format therefore pins `ktfmt("0.53")` to preserve the prior formatter output without source edits.

Kotlin 2.4.20-RC2 also rejects an existing nullable outer result declaration in
`FailFastStrategies.failFastForEachBatchOfBatch1`: `fold<Either<...>?>` is immediately followed by
the non-null receiver call `mapLeft`. Both fold branches already return a non-null `Either`, so the
authorized build-owned source compatibility correction removes only that outer `?`, preserving
all nullable payload arguments, branch logic, and the existing progressive compiler policy.
All other dependency and plugin versions remain pinned.

The hardcoded JUnit 5.10.2 suite version will be removed in favor of the existing catalog version,
5.12.0. Unused JUnit aliases may be removed after confirming they have no consumers.

## `AGENTS.md`

A new root `AGENTS.md` will adapt ReVoman's concise structure to Vador. It will be self-contained
because this repository does not have ReVoman's `DEVELOPMENT.md` and `STYLE.md` split.

It will cover:

- Vador's purpose, Java 25 requirement, and Gradle-wrapper rule;
- the `vador`, `matchers`, `build-logic`, version-catalog, Detekt, and generated-source locations;
- canonical full-build, formatting, module-test, targeted-test, coverage, and publication-model
  commands;
- functional Kotlin style, Java-consumer compatibility, and existing formatting conventions;
- ownership rules for version catalogs, convention plugins, and module-specific configuration;
- the Immutables/KAPT generated-source boundary;
- configuration-cache and signing constraints;
- a prohibition on publishing or editing generated sources without explicit authorization.

The guide will not copy ReVoman-specific integration tests, Docker/Qodana workflows, logging
requirements, benchmarks, or Core-consumption instructions.

## Failure Handling

Failures reproduced on the pre-migration revision will be recorded separately and will not justify
unrelated source or dependency changes. Failures introduced by the migration are regressions and
must be corrected within the owning settings or convention plugin.

If a third-party execution task is incompatible with configuration-cache serialization, it may be
marked incompatible at the typed task level with a precise reason. The build will not restore
global warning mode. Formatter or plugin classloader failures will first be diagnosed against the
pinned version; toolchain changes and global cache disabling are not acceptable workarounds.

Credential-dependent publishing and endpoint-dependent Sonar uploads will not run. Their local
task models, generated metadata, inputs, and dependencies will be validated without contacting the
external service.

## Verification

Verification will proceed from the smallest model to the complete build:

1. Record the Gradle 8.14.1 full-build baseline before changing build configuration.
2. Confirm `./gradlew --version` reports Gradle 9.7.1 on Java 25.
3. Build the standalone included build.
4. Run root `help` and `projects` with `--warning-mode all` and configuration-cache problems set to
   fail.
5. Repeat a model command and confirm configuration-cache reuse.
6. Run `./gradlew clean build --warning-mode all --console=plain`.
7. Confirm tests, Spotless, Detekt, Kover, KAPT, Javadoc, and sources-JAR tasks complete.
8. Confirm the root Kover XML and merged Detekt XML reports exist at the Sonar-configured paths.
9. Generate Maven POM and Gradle module metadata for both publications without publishing.
10. Inspect artifact names and source/Javadoc JAR contents, including generated Immutables sources.
11. Inspect the final diff and status and confirm no unrelated tracked files changed.

The migration is complete when the Gradle 9.7.1/Java 25 build passes, strict configuration-cache
storage and reuse work for supported workflows, both publications remain structurally compatible,
the reports consumed by Sonar exist, `AGENTS.md` accurately describes the repository, and the
working tree contains only intended changes.
