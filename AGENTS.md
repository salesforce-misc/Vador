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
- Keep dependency versions and project-plugin versions in `gradle/libs.versions.toml`.
- Settings-bootstrap plugins may remain explicitly pinned in `settings.gradle.kts`.
- Keep shared build policy in the focused convention under `build-logic`; module scripts own only module-specific configuration.

## Generated Sources

- Immutables Java sources under `vador/build/generated/source/kapt/` are generated outputs.
- Never edit or commit generated sources.
- Changes to KAPT inputs must preserve generated sources in the published sources JAR and Javadoc inputs.

## Gradle and Publishing

- Configuration-cache problems fail the build. Use a typed task-level incompatibility declaration only for a reproduced third-party limitation.
- Validate publications with `generatePomFileForVadorPublication` and `generateMetadataFileForVadorPublication` tasks.
- Do not run Maven Central publishing, signing, Maven Local publication, or Sonar upload tasks without explicit authorization and the required credentials.
