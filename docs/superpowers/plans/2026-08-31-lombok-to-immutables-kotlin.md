# Lombok-to-Immutables Kotlin Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove Lombok and delombok from Vador, make all handwritten production source Kotlin, and preserve the supported Java fluent DSL through Immutables-generated public values and builders.

**Architecture:** Kotlin abstract model inputs and public Kotlin base classes define state and behavior. Immutables 2.12.2 runs through KAPT and generates the existing public Java class names, JavaBean getters, immutable storage, builders, singular collection methods, and `toBuilder()` copies. Small Kotlin builder contracts retain compound legacy singular aliases that Immutables cannot infer.

**Tech Stack:** Kotlin 2.1.10, Java 17 toolchain, Gradle 8.14.1, Kotlin KAPT, Immutables 2.12.2, JUnit Jupiter, AssertJ, Kover, Spotless, Detekt.

**Spec:** [Approved Lombok-to-Immutables Kotlin migration design](../specs/2026-08-31-lombok-to-immutables-kotlin-design.md)

## Global Constraints

- Preserve every existing Gradle, Kotlin, JDK, plugin, dependency, wrapper, and SDK version. Add only Immutables 2.12.2 and the Kotlin KAPT plugin declaration matching Kotlin 2.1.10.
- Preserve Java source compatibility, not binary compatibility. Do not reproduce Lombok nested builder names or generic signatures.
- Preserve `toValidate()`/`prepare()`, `check()`/`done()`, `toBuilder()`, JavaBean getters, all 35 singular-method occurrences, immutable snapshots, helper methods, and domain validation messages.
- Do not expose `Abstract*` generator inputs in documentation or supported APIs. Mark them Kotlin `internal` even though they remain JVM-visible.
- Use abstract Kotlin classes, not interfaces, for model bases with concrete behavior.
- Put all handwritten production Kotlin under `vador/src/main/kotlin`. Do not check generated Java into Git.
- Keep at least one permanent Java consumer-contract test under `vador/src/test/java`.
- Keep the worktree buildable at every commit. Migrate each inheritance family in one commit.
- Treat the approved exception change for an omitted required attribute as semantic compatibility: incomplete builders must fail, but the exception may change from `NullPointerException` to `IllegalStateException`.
- The approved design record is the sole allowed operational search exception for the word `Lombok` after the migration.

---

### Task 1: Freeze the supported Java DSL before changing generators

**Files:**

- Create: `vador/src/test/java/com/salesforce/vador/compatibility/JavaDslCompatibilityTest.java`

This is a characterization task. The new test must be green against the current Lombok implementation; do not manufacture a failing baseline.

- [ ] **Step 1: Add representative compile-time Java DSL chains**

Add a JUnit Jupiter test class with small handwritten `Bean` and `Container` fixtures. Compile and execute these exact usage shapes:

```java
final var fieldConfig =
    FieldConfig.<String, Bean, String>toValidate()
        .withFieldValidator(value -> !value.isBlank())
        .shouldHaveValidFormatOrFailWith(Bean::getText, "bad-format")
        .prepare();

final var spec =
    Spec1.<Bean, String, String>check()
        .given(Bean::getText)
        .shouldMatch(equalTo("valid"))
        .orFailWith("bad-spec")
        .done();

final var validationConfig =
    ValidationConfig.<Bean, String>toValidate()
        .shouldHaveFieldOrFailWith(Bean::getText, "required")
        .withSpec(factory ->
            factory.<String>_1()
                .given(Bean::getText)
                .shouldMatch(equalTo("valid"))
                .orFailWith("bad-spec"))
        .prepare();

final var batchConfig =
    BatchValidationConfig.<Bean, String>toValidate()
        .findAndFilterDuplicatesConfig(
            FilterDuplicatesConfig.<Bean, String>toValidate()
                .findAndFilterDuplicatesWith(Bean::getText))
        .prepare();

final var containerConfig =
    ContainerValidationConfig.<Container, String>toValidate()
        .withBatchMember(Container::getBeans)
        .prepare();
```

Assert JavaBean getters on the built objects, public base-class assignability, value equality/hash code, and an informative nonblank `toString()`.

- [ ] **Step 2: Characterize independent copying and required-value rejection**

Add assertions that:

```java
final var copied =
    fieldConfig.toBuilder()
        .absentOrHaveValidFormatOrFailWith(Bean::getText, "optional-format")
        .prepare();
```

does not mutate `fieldConfig`, and that building `Spec1.check().orFailWith("failure").done()` without `given` throws a `RuntimeException`. Do not assert its exact subclass.

- [ ] **Step 3: Assert every singular occurrence by builder and method name**

Add a helper that obtains `builder.getClass().getMethods()`, maps to method names, and uses AssertJ `contains` with the exact strings in this matrix:

| Builder | Singular method names |
|---|---|
| `FieldConfig.toValidate()` | `shouldHaveValidFormatOrFailWith`, `shouldHaveValidFormatOrFailWithFn`, `absentOrHaveValidFormatOrFailWith`, `absentOrHaveValidFormatOrFailWithFn` |
| `IDConfig.toValidate()` | `shouldHaveValidSFIdFormatOrFailWith`, `shouldHaveValidSFPolymorphicIdFormatOrFailWith`, `shouldHaveValidSFIdFormatOrFailWithFn`, `shouldHaveValidSFPolymorphicIdFormatOrFailWithFn`, `absentOrHaveValidSFIdFormatOrFailWith`, `absentOrHaveValidSFPolymorphicIdFormatOrFailWith`, `absentOrHaveValidSFIdFormatOrFailWithFn`, `absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn` |
| `ValidationConfig.toValidate()` | `shouldHaveFieldOrFailWith`, `shouldHaveFieldOrFailWithFn`, `withIdConfig`, `withFieldConfig`, `withSpec`, `withValidatorEtr`, `withValidator` |
| `BatchValidationConfig.toValidate()` | `findAndFilterDuplicatesConfig` |
| `ContainerValidationConfig.toValidate()` | `withContainerValidatorEtr`, `withContainerValidator`, `withBatchMember` |
| `ContainerValidationConfigWith2Levels.toValidate()` | `withBatchMember` |
| `Spec1.check()` | `shouldMatchField`, `shouldMatch` |
| `Spec2.check()` | `matches`, `shouldMatch`, `shouldRelateWithEntry` |
| `Spec3.check()` | `matches`, `shouldRelateWithEntry`, `orField1ShouldMatch`, `orField2ShouldMatch` |
| `Spec4.check()` | `whenFieldMatches`, `thenFieldShouldMatch` |

The table covers all 35 source occurrences; inherited builder methods are asserted on the concrete builders consumers actually receive.

- [ ] **Step 4: Run the baseline contract and module tests**

Run:

```bash
./gradlew :vador:test --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest --no-daemon --console=plain
./gradlew :vador:test --no-daemon --console=plain
```

Expected: both commands pass on the Lombok baseline.

- [ ] **Step 5: Commit the compatibility baseline**

```bash
git add vador/src/test/java/com/salesforce/vador/compatibility/JavaDslCompatibilityTest.java
git commit -m "test: freeze the public Java builder DSL"
```

---

### Task 2: Add KAPT/Immutables and normalize the production Kotlin layout

**Files:**

- Modify: `libs.versions.toml`
- Modify: `gradle.properties`
- Modify: `vador/build.gradle.kts`
- Create: `vador/src/main/kotlin/com/salesforce/vador/immutables/CodeGeneration.kt`
- Move: every existing `vador/src/main/java/**/*.kt` file to the same package path under `vador/src/main/kotlin`

- [ ] **Step 1: Record a green compilation before moving sources**

Run:

```bash
./gradlew :vador:compileKotlin :vador:compileJava --no-daemon --console=plain
```

Expected: PASS. This is the pre-refactor control.

- [ ] **Step 2: Add only the approved catalog entries**

Add:

```toml
[versions]
immutables = "2.12.2"

[libraries]
immutables-value = { module = "org.immutables:value", version.ref = "immutables" }
immutables-value-annotations = { module = "org.immutables:value-annotations", version.ref = "immutables" }

[plugins]
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
```

Do not change any existing catalog value yet; Lombok remains temporarily while Java model families still exist.

- [ ] **Step 3: Enable KAPT beside the temporary Lombok pipeline**

In `vador/build.gradle.kts`, add `alias(libs.plugins.kotlin.kapt)` and:

```kotlin
dependencies {
  kapt(libs.immutables.value)
  compileOnly(libs.immutables.value.annotations)
}
```

Temporarily change the delombok bridge to include normal Kotlin sources:

```kotlin
if (!System.getProperty("idea.sync.active").toBoolean()) {
  kotlin.sourceSets.main { kotlin.setSrcDirs(listOf("src/main/kotlin", tasks.delombok)) }
}
```

Add to `gradle.properties`:

```properties
kapt.use.k2=true
```

- [ ] **Step 4: Add the two generator styles and null-entry marker**

Create `CodeGeneration.kt` with:

```kotlin
package com.salesforce.vador.immutables

import org.immutables.value.Value

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class AllowNulls

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Value.Style(
  typeImmutable = "*",
  typeAbstract = ["Abstract*"],
  builder = "toValidate",
  build = "prepare",
  toBuilder = "toBuilder",
  depluralize = true,
  depluralizeDictionary =
    [
      "withIdConfig:withIdConfigs",
      "withFieldConfig:withFieldConfigs",
      "withSpec:withSpecs",
      "withValidatorEtr:withValidatorEtrs",
      "findAndFilterDuplicatesConfig:findAndFilterDuplicatesConfigs",
      "withContainerValidatorEtr:withContainerValidatorEtrs",
      "withBatchMember:withBatchMembers",
    ],
  add = "*",
  put = "*",
  visibility = Value.Style.ImplementationVisibility.PUBLIC,
)
annotation class ConfigStyle

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Value.Style(
  typeImmutable = "*",
  typeAbstract = ["Abstract*"],
  builder = "check",
  build = "done",
  toBuilder = "toBuilder",
  depluralize = true,
  add = "*",
  put = "*",
  visibility = Value.Style.ImplementationVisibility.PUBLIC,
)
annotation class SpecStyle
```

- [ ] **Step 5: Move the 27 existing Kotlin production files without changing packages**

Move every path returned by:

```bash
rg --files vador/src/main/java | rg '\.kt$' | sort
```

from the `vador/src/main/java` root to the identical package-relative suffix under `vador/src/main/kotlin`. Preserve content in this step so path movement remains reviewable.

- [ ] **Step 6: Compile with both processors active**

Run:

```bash
./gradlew clean :vador:kaptKotlin :vador:compileKotlin :vador:compileJava --no-daemon --console=plain
./gradlew :vador:test --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest --no-daemon --console=plain
```

Expected: PASS. The accepted K2 KAPT alpha and non-incremental-processor warnings may appear; compilation errors may not.

- [ ] **Step 7: Commit the code-generation foundation**

```bash
git add libs.versions.toml gradle.properties vador/build.gradle.kts vador/src/main
git commit -m "build: add Immutables KAPT foundation"
```

---

### Task 3: Migrate the complete specification family atomically

**Files:**

- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/base/BaseSpec.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/Spec1.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/Spec2.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/Spec3.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/Spec4.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/specs/Spec5.java`
- Delete: `vador/src/main/java/com/salesforce/vador/specs/factory/SpecFactory.java`
- Create: matching `.kt` files under `vador/src/main/kotlin`
- Create: `vador/src/main/kotlin/com/salesforce/vador/specs/specs/base/SpecBuilder.kt`
- Create: `vador/src/main/kotlin/com/salesforce/vador/specs/specs/SpecBuilderDsl.kt`
- Modify: `vador/src/main/kotlin/com/salesforce/vador/types/Specs.kt`
- Verify unchanged behavior in: `vador/src/main/kotlin/com/salesforce/vador/specs/specs/SpecEx.kt`

- [ ] **Step 1: Add a generator-oriented failing compile sentinel**

Before deleting Java, change only `Specs.kt` to refer to the not-yet-created common protocol:

```kotlin
fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, SpecBuilder<ValidatableT, FailureT>>

fun interface Specs<ValidatableT, FailureT> :
  Function1<
    SpecFactory<ValidatableT, FailureT>,
    Collection<SpecBuilder<ValidatableT, FailureT>>,
  >
```

Run:

```bash
./gradlew :vador:compileKotlin --no-daemon --console=plain
```

Expected: FAIL because `SpecBuilder` does not exist. This proves the production code depends on the new generator-neutral seam.

- [ ] **Step 2: Add the common terminal protocol**

Create `SpecBuilder.kt`:

```kotlin
package com.salesforce.vador.specs.specs.base

interface SpecBuilder<ValidatableT, FailureT> {
  fun done(): BaseSpec<ValidatableT, FailureT>
}
```

This replaces the internal use of `BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>` without promising a generated nested builder ABI.

- [ ] **Step 3: Translate `BaseSpec` one-for-one**

Create a public abstract Kotlin class with:

- `abstract val nameForTest: String?`
- `abstract val orFailWith: FailureT?`
- `abstract fun toPredicate(): Predicate<ValidatableT?>`
- the exact existing `INVALID_FAILURE_CONFIG` constant exposed through `companion object` and `@JvmField`
- the existing `getFailure(validatable)` behavior, marked `@Value.NonAttribute`

Keep `toPredicate()` abstract. Mark every zero-argument concrete helper that is not storage with `@Value.NonAttribute`.

- [ ] **Step 4: Add reusable alias contracts**

In `SpecBuilderDsl.kt`, create one generic interface per generated builder and delegate the exact legacy alias to the generated single-element map/collection method:

| Generated initializer | Legacy default method | Applies to |
|---|---|---|
| `shouldMatchAnyOfFields(element)` | `shouldMatchField(element)` | `Spec1.Builder` |
| `shouldMatchAnyOf(element)` | `shouldMatch(element)` | `Spec1.Builder`, `Spec2.Builder` |
| `matchesAnyOf(element)` | `matches(element)` | `Spec2.Builder`, `Spec3.Builder` |
| `shouldRelateWith(key, value)` | `shouldRelateWithEntry(key, value)` | `Spec2.Builder`, `Spec3.Builder` |
| `orField1ShouldMatchAnyOf(element)` | `orField1ShouldMatch(element)` | `Spec3.Builder` |
| `orField2ShouldMatchAnyOf(element)` | `orField2ShouldMatch(element)` | `Spec3.Builder` |
| `whenTheseFieldsMatch(key, value)` | `whenFieldMatches(key, value)` | `Spec4.Builder` |
| `thenThoseFieldsShouldMatch(key, value)` | `thenFieldShouldMatch(key, value)` | `Spec4.Builder` |

Each method returns its corresponding generated `Spec1.Builder` through `Spec4.Builder` self type. Follow the proven form:

```kotlin
interface Spec1BuilderDsl<ValidatableT, GivenT, SELF> {
  fun shouldMatchAnyOfFields(element: Function1<ValidatableT, *>): SELF

  fun shouldMatchField(element: Function1<ValidatableT, *>): SELF =
    shouldMatchAnyOfFields(element)

  fun shouldMatchAnyOf(element: Matcher<out GivenT>): SELF

  fun shouldMatch(element: Matcher<out GivenT>): SELF = shouldMatchAnyOf(element)
}
```

Use the original Java attribute types for the other contracts. Do not broaden nullability or replace Vavr functional interfaces with Kotlin functions.

- [ ] **Step 5: Create the five `AbstractSpec*` inputs**

For every input, apply `@SpecStyle` and `@Value.Immutable(copy = false)`, extend `BaseSpec`, retain the exact behavior body, and declare a nested abstract `Builder` implementing `SpecBuilder` plus its alias contract.

Use this exact state map:

| Input | Type parameters | Required/nullable attributes |
|---|---|---|
| `AbstractSpec1` | `ValidatableT, FailureT, GivenT` | nonnull `given`; empty `shouldMatchAnyOfFields`; empty `shouldMatchAnyOf`; nullable `orFailWithFn` |
| `AbstractSpec2` | `ValidatableT, FailureT, WhenT, ThenT` | nonnull `when`; empty `matchesAnyOf`; nonnull `then`; empty `shouldMatchAnyOf`; empty `shouldRelateWith`; nullable `shouldRelateWithFn`; nullable `orFailWithFn` |
| `AbstractSpec3` | `ValidatableT, FailureT, WhenT, Then1T, Then2T` | nonnull `when`, `thenField1`, `thenField2`; empty `matchesAnyOf`, `shouldRelateWith`, `orField1ShouldMatchAnyOf`, `orField2ShouldMatchAnyOf`; nullable `shouldRelateWithFn`, `orFailWithFn` |
| `AbstractSpec4` | `ValidatableT, FailureT` | empty nonnull maps `whenTheseFieldsMatch`, `thenThoseFieldsShouldMatch`; nullable `orFailWithFn` |
| `AbstractSpec5` | `ValidatableT, FailureT` | nonnull tuples `whenAllTheseFieldsMatch`, `thenAllThoseFieldsShouldMatch`; nullable `orFailWithFn` |

Preserve calls to `SpecEx.toPredicateEx`, `SpecEx.getFailureEx`, and the current failure-selection logic exactly. Apply `@Value.NonAttribute` to concrete `toPredicate()` and `getFailure()` overrides so KAPT never interprets behavior as state.

- [ ] **Step 6: Convert `SpecFactory` to Kotlin while preserving Java names**

Implement public methods `_1()`, `_2()`, `_3()`, `_4()`, and `_5()` returning `Spec1.Builder`, `Spec2.Builder`, `Spec3.Builder`, `Spec4.Builder`, and `Spec5.Builder`, respectively, with the same generic parameter order. Use `@Suppress("FunctionName")` rather than changing names.

- [ ] **Step 7: Prove the generated spec API**

Run:

```bash
./gradlew clean :vador:test \
  --tests com.salesforce.vador.specs.factory.SpecFactoryTest \
  --tests 'com.salesforce.vador.execution.spec.*' \
  --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --no-daemon --console=plain
```

Expected: PASS. Inspect the public generated methods:

```bash
javap -classpath vador/build/classes/kotlin/main:vador/build/classes/java/main -public \
  com.salesforce.vador.specs.specs.Spec1 \
  'com.salesforce.vador.specs.specs.Spec1$Builder' \
  com.salesforce.vador.specs.specs.Spec4
```

Expected output includes `check`, `done`, `toBuilder`, JavaBean getters, and the legacy aliases asserted by the contract test.

- [ ] **Step 8: Commit the atomic spec-family migration**

```bash
git add vador/src/main vador/src/test/java/com/salesforce/vador/compatibility/JavaDslCompatibilityTest.java
git commit -m "refactor: generate the specification DSL with Immutables"
```

---

### Task 4: Migrate leaf configuration values and their internal builder references

**Files:**

- Delete: `vador/src/main/java/com/salesforce/vador/config/FieldConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/IDConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/FilterDuplicatesConfig.java`
- Create: matching `.kt` files under `vador/src/main/kotlin/com/salesforce/vador/config`
- Create: `vador/src/main/kotlin/com/salesforce/vador/config/ConfigBuilderDsl.kt`
- Modify: `vador/src/main/java/com/salesforce/vador/config/base/BaseValidationConfig.java`
- Modify: `vador/src/main/java/com/salesforce/vador/config/base/BaseBatchValidationConfig.java`
- Modify: `vador/src/main/kotlin/com/salesforce/vador/execution/strategies/util/ConfigToValidators.kt`
- Modify: `vador/src/main/kotlin/com/salesforce/vador/execution/strategies/util/Utils.kt`

- [ ] **Step 1: Make the internal builder references fail against the old generator**

Change the four internal imports/types from Lombok names to:

```kotlin
FieldConfig.Builder<FieldT, ValidatableT, FailureT>
IDConfig.Builder<IDT, ValidatableT, FailureT, EntityIdInfoT>
FilterDuplicatesConfig.Builder<ValidatableT, FailureT?>
```

and equivalent Java nested builder types in the two remaining Java bases. Run:

```bash
./gradlew :vador:compileKotlin :vador:compileJava --no-daemon --console=plain
```

Expected: FAIL because the old generator does not provide nested `Builder` classes.

- [ ] **Step 2: Add configuration alias contracts**

In `ConfigBuilderDsl.kt`, use generic self types and exact original Vavr/reflection-util types to delegate:

| Generated initializer | Legacy default method | Applies to |
|---|---|---|
| `shouldHaveValidFormatForAllOrFailWith(key, value)` | `shouldHaveValidFormatOrFailWith(key, value)` | `FieldConfig.Builder` |
| `absentOrHaveValidFormatForAllOrFailWith(key, value)` | `absentOrHaveValidFormatOrFailWith(key, value)` | `FieldConfig.Builder` |
| `shouldHaveValidSFIdFormatForAllOrFailWith(key, value)` | `shouldHaveValidSFIdFormatOrFailWith(key, value)` | `IDConfig.Builder` |
| `shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(key, value)` | `shouldHaveValidSFPolymorphicIdFormatOrFailWith(key, value)` | `IDConfig.Builder` |
| `absentOrHaveValidSFIdFormatForAllOrFailWith(key, value)` | `absentOrHaveValidSFIdFormatOrFailWith(key, value)` | `IDConfig.Builder` |
| `absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(key, value)` | `absentOrHaveValidSFPolymorphicIdFormatOrFailWith(key, value)` | `IDConfig.Builder` |

The six function-valued singular methods (`shouldHaveValidFormatOrFailWithFn`, `absentOrHaveValidFormatOrFailWithFn`, and the four corresponding SF ID methods) already equal their attribute names and are generated directly; do not add duplicate defaults for them.

- [ ] **Step 3: Create the three immutable inputs**

Apply `@ConfigStyle` and `@Value.Immutable(copy = false)` to `internal abstract class AbstractFieldConfig`, `AbstractIDConfig`, and `AbstractFilterDuplicatesConfig`. Preserve every generic parameter and property type from the Java source.

Use nullable Kotlin types only where the source has `@Nullable`. Add `@get:AllowNulls` to map attributes whose keys or values permit null. Keep singular maps/collections nonnull and empty by default.

Each `AbstractFieldConfig` and `AbstractIDConfig` declares a nested abstract `Builder` implementing its alias contract with `FieldConfig.Builder<FieldT, ValidatableT, FailureT>` or `IDConfig.Builder<IDT, ValidatableT, FailureT, EntityIdInfoT>` as `SELF`. `AbstractFilterDuplicatesConfig` needs no custom builder contract.

- [ ] **Step 4: Update remaining Java/Kotlin internals to generated builder types**

Use these exact replacements:

| Old type | New type |
|---|---|
| `FieldConfig.FieldConfigBuilder<FieldT, ValidatableT, FailureT>` | `FieldConfig.Builder<FieldT, ValidatableT, FailureT>` |
| `IDConfig.IDConfigBuilder<IDT, ValidatableT, FailureT, EntityIdInfoT>` | `IDConfig.Builder<IDT, ValidatableT, FailureT, EntityIdInfoT>` |
| `FilterDuplicatesConfig.FilterDuplicatesConfigBuilder<ValidatableT, FailureT>` | `FilterDuplicatesConfig.Builder<ValidatableT, FailureT>` |

Keep builder storage rather than eagerly building values; current lazy `prepare()` behavior must remain unchanged.

- [ ] **Step 5: Run focused leaf-config and compatibility tests**

```bash
./gradlew clean :vador:test \
  --tests com.salesforce.vador.execution.config.IDConfigTest \
  --tests com.salesforce.vador.execution.config.nested.FieldConfigTest \
  --tests com.salesforce.vador.execution.UtilsTest \
  --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --no-daemon --console=plain
```

Expected: PASS, including nullable failure values and every leaf singular alias.

- [ ] **Step 6: Commit the leaf-value migration**

```bash
git add vador/src/main
git commit -m "refactor: generate leaf configs with Immutables"
```

---

### Task 5: Migrate the inherited validation configuration family atomically

**Files:**

- Delete: `vador/src/main/java/com/salesforce/vador/config/base/BaseValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/base/BaseBatchValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/ValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/BatchValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/BatchOfBatch1ValidationConfig.java`
- Create: matching `.kt` files under `vador/src/main/kotlin`
- Modify: `vador/src/main/kotlin/com/salesforce/vador/config/ConfigBuilderDsl.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/compatibility/KotlinDslCompatibilityTest.kt`
- Verify unchanged behavior in: `vador/src/main/kotlin/com/salesforce/vador/config/base/BaseValidationConfigEx.kt`

- [ ] **Step 1: Add the base legacy alias contract**

Add the proven generic contract:

```kotlin
interface ValidationBuilderDsl<ValidatableT, FailureT, SELF> {
  fun shouldHaveFieldsOrFailWith(
    key: TypedPropertyGetter<ValidatableT, *>?,
    failure: FailureT?,
  ): SELF

  fun shouldHaveFieldOrFailWith(
    key: TypedPropertyGetter<ValidatableT, *>?,
    failure: FailureT?,
  ): SELF = shouldHaveFieldsOrFailWith(key, failure)
}
```

The generated builder directly supplies `shouldHaveFieldOrFailWithFn`, `withIdConfig`, `withFieldConfig`, `withSpec`, `withValidatorEtr`, and `withValidator` through its configured `put`, `add`, and depluralization rules.

- [ ] **Step 2: Translate the two public bases**

`BaseValidationConfig<ValidatableT, FailureT>` must declare exactly these abstract properties:

```text
shouldHaveFieldsOrFailWith
shouldHaveFieldsOrFailWithFn
shouldHaveFieldOrFailWithFn
withIdConfigs
withFieldConfigs
specify
withSpecs
withValidatorEtrs
withValidators
forAnnotations
withValidator
withRecursiveMapper
```

Use `IDConfig.Builder` and `FieldConfig.Builder` for stored child builders. Preserve nullable scalar/tuple properties, but make singular collections/maps nonnull empty collections.

Keep `getSpecs`, `getPredicateOfSpecForTest`, `getRequiredFieldNames`, and `getValidatableType` as concrete methods delegating to `BaseValidationConfigEx`; annotate them `@Value.NonAttribute`.

`BaseBatchValidationConfig` extends it and adds nonnull empty `findAndFilterDuplicatesConfigs: Collection<FilterDuplicatesConfig.Builder<ValidatableT, FailureT?>>`.

- [ ] **Step 3: Add the three generated concrete inputs**

Apply `@ConfigStyle` and `@Value.Immutable(copy = false)` to:

- `internal abstract class AbstractValidationConfig<ValidatableT, FailureT>`
- `internal abstract class AbstractBatchValidationConfig<ValidatableT, FailureT>`
- `internal abstract class AbstractBatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT>`

Each nested abstract `Builder` implements `ValidationBuilderDsl` with its own generated `*.Builder` self type. The batch-of-batch input preserves the required `withMemberBatchValidationConfig` tuple and its nullable nested batch failure type.

- [ ] **Step 4: Compile and run the inherited behavior tests**

Create `KotlinDslCompatibilityTest.kt` before running the tests. It must construct a `Spec1` with `check()`/`done()`, construct a `ValidationConfig` with `toValidate()`/`prepare()`, read JavaBean-backed Kotlin properties, and make an independent `toBuilder()` copy. Use generated public types, not `Abstract*` inputs.

```bash
./gradlew clean :vador:test \
  --tests com.salesforce.vador.execution.config.BaseValidationConfigTest \
  --tests com.salesforce.vador.execution.config.nested.BatchOfBatch1ValidationConfigTest \
  --tests com.salesforce.vador.execution.VadorTest \
  --tests com.salesforce.vador.execution.VadorBatchTest \
  --tests com.salesforce.vador.compatibility.KotlinDslCompatibilityTest \
  --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --no-daemon --console=plain
```

Expected: PASS. `ValidationConfig`, `BatchValidationConfig`, and `BatchOfBatch1ValidationConfig` remain assignable to their public Kotlin base classes.

- [ ] **Step 5: Commit the atomic inherited-config migration**

```bash
git add vador/src/main vador/src/test/kotlin/com/salesforce/vador/compatibility/KotlinDslCompatibilityTest.kt
git commit -m "refactor: generate inherited configs with Immutables"
```

---

### Task 6: Migrate the container configuration family atomically

**Files:**

- Delete: `vador/src/main/java/com/salesforce/vador/config/base/BaseContainerValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/container/ContainerValidationConfig.java`
- Delete: `vador/src/main/java/com/salesforce/vador/config/container/ContainerValidationConfigWith2Levels.java`
- Create: matching `.kt` files under `vador/src/main/kotlin`
- Verify unchanged behavior in: `vador/src/main/kotlin/com/salesforce/vador/config/container/ContainerValidationConfigEx.kt`

- [ ] **Step 1: Translate the public container base**

Declare abstract properties matching the Java source:

- nullable `shouldHaveMinBatchSizeOrFailWith`
- nullable `shouldHaveMaxBatchSizeOrFailWith`
- nonnull empty `withContainerValidatorEtrs`
- nullable `withContainerValidators`
- nonnull empty `withContainerValidator`

Keep `getContainerValidators()` as a concrete `@Value.NonAttribute` delegate to `ContainerValidationConfigEx.getContainerValidatorsEx`.

- [ ] **Step 2: Create the two immutable container inputs**

Apply `@ConfigStyle` and `@Value.Immutable(copy = false)` to `AbstractContainerValidationConfig` and `AbstractContainerValidationConfigWith2Levels`.

Preserve:

- empty `withBatchMembers` collections with the original `TypedPropertyGetter` generic types;
- required `withScopeOf1LevelDeep` on the two-level type;
- `getFieldNamesForBatch`, `getFieldNamesForBatchLevel1`, and `getFieldNamesForBatchRootLevel` as `@Value.NonAttribute` behavior.

No custom builder contract is needed: `withContainerValidator`, `withContainerValidatorEtr`, and `withBatchMember` are covered by style configuration.

- [ ] **Step 3: Run focused container tests and the Java contract**

```bash
./gradlew clean :vador:test \
  --tests com.salesforce.vador.execution.config.ContainerValidationConfigTest \
  --tests com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsTest \
  --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --no-daemon --console=plain
```

Expected: PASS.

- [ ] **Step 4: Prove there is no handwritten production Java left**

```bash
find vador/src/main -type f -name '*.java' -print
```

Expected: no output. Generated Java remains only under `vador/build/generated/source/kapt/main`.

- [ ] **Step 5: Commit the atomic container migration**

```bash
git add vador/src/main
git commit -m "refactor: generate container configs with Immutables"
```

---

### Task 7: Replace ordinary Lombok test values with concise Kotlin fixtures

**Files:**

- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/UtilsTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/VadorAnnotationTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/VadorBatchTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/VadorTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/BaseValidationConfigTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/ContainerValidationConfigTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/IDConfigTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/nested/BatchOfBatch1ValidationConfigTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/nested/ContainerValidationConfigWith2LevelsTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/config/nested/FieldConfigTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/spec/Spec1TestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/spec/Spec2TestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/spec/Spec3TestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/spec/Spec4TestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/execution/spec/Spec5TestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/lift/AggregationLiftUtilTestFixtures.kt`
- Create: `vador/src/test/kotlin/com/salesforce/vador/specs/factory/SpecFactoryTestFixtures.kt`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/UtilsTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/VadorAnnotationTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/VadorBatchTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/VadorTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/BaseValidationConfigTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/ContainerValidationConfigTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/IDConfigTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/nested/BatchOfBatch1ValidationConfigTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/nested/ContainerValidationConfigWith2LevelsTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/config/nested/FieldConfigTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/spec/Spec1Test.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/spec/Spec2Test.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/spec/Spec3Test.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/spec/Spec4Test.java`
- Modify: `vador/src/test/java/com/salesforce/vador/execution/spec/Spec5Test.java`
- Modify: `vador/src/test/java/com/salesforce/vador/lift/AggregationLiftUtilTest.java`
- Modify: `vador/src/test/java/com/salesforce/vador/specs/factory/SpecFactoryTest.java`

- [ ] **Step 1: Add Kotlin data fixtures for execution and spec tests**

Create one test-named `Fixtures.kt` file per Java test and prefix types with the test name to avoid package collisions. Translate Lombok `@Value` to a Kotlin `data class` whose primary-constructor properties are `val`, preserving JavaBean getters. Use this exact map:

| Java test | Kotlin fixtures and properties |
|---|---|
| `VadorBatchTest` | `VadorBatchBean(id)`, `VadorBatchRecursiveBean(id, recursiveBeans)` |
| `VadorTest` | `VadorBean(value)`, `VadorRecursiveBean(id, recursiveBeans)` |
| `UtilsTest` | `UtilsBean(id)`, `UtilsMultiKeyBean(id1, id2)` |
| `Spec1Test` | `Spec1Bean1(value)`, `Spec1Bean2(field1, field2)`, `Spec1Bean3(field1, field2, field3)` |
| `Spec2Test` | `Spec2Bean(value, valueStr, dependentValue1, dependentValue2)`, `Spec2Bean2(bt, valueStr)`, plus top-level `Spec2BillingTerm` |
| `Spec3Test` | `Spec3DatesBean(compareDates, date1, date2)`, `Spec3Bean(compareFields, bdom, startDate)` |
| `Spec4Test` | `Spec4Bean(whenField1, whenField2, whenField3, thenField1, thenField2, thenField3)`, `Spec4Field(id)` |
| `Spec5Test` | `Spec5Bean1(whenField1, whenField2, whenField3, thenField1, thenField2, thenField3)`, `Spec5Bean2(whenField1, whenField2, thenField1, thenField2)`, `Spec5Field(id)` |
| `SpecFactoryTest` | `SpecFactoryBean(value)` |
| `AggregationLiftUtilTest` | `AggregationContainer(member)`, `AggregationMember(id)` |

Preserve every original nullable type. Update constructor calls and Java method references to the prefixed class names; do not change assertions or production DSL calls.

- [ ] **Step 2: Add Kotlin fixtures for annotation tests**

Create `VadorAnnotationFixtures.kt` with `data class` values matching the eight existing classes. Put annotations on backing fields with `@field:`:

```kotlin
internal data class AnnotationBean(
  @field:Positive(failureKey = "unexpectedException") val idOne: Int,
  @field:Negative(failureKey = "unexpectedException") val idTwo: Int,
  @field:NonNegative(failureKey = "unexpectedException") val idThree: Int,
)
```

Apply the same rule to `BeanMix`, `BeanCustom`, `BeanCustom2`, `BeanCustom3`, `BeanInt`, `BeanFailure`, and generic `BeanRequired<T>`. Reference the existing nested Java validator/annotation classes with Kotlin class literals; preserve runtime field annotations.

- [ ] **Step 3: Add Kotlin fixtures for nested config tests**

Create prefixed data classes for:

- `BatchOfBatch1ValidationConfigTest`: bean `(value, label)`, item `(id, beanBatch)`, root `(itemsBatch)`.
- `ContainerValidationConfigTest`: two empty bean types, multi-batch container `(batch1, batch2)`, nested bean, level-one container `(beanBatch)`, root `(containerLevel1Batch)`, pair container `(id, beanBatch)`.
- `ContainerValidationConfigWith2LevelsTest`: bean, level two `(id, beanBatch)`, level one `(id, containerLevel2Batch)`, root `(containerLevel1Batch)`, level-one multi-batch `(containerLevel2Batch, beanBatch)`, root multi-container `(containerLevel1Batch1, containerLevel1Batch2)`.
- `BaseValidationConfigTest`: ID-string bean `(requiredField, accountId, contactId)`, flat bean `(requiredField1, requiredField2, sfId1, sfId2, requiredList)`, container bean `(requiredField, bean)`, optional bean `(str)`.
- `FieldConfigTest`: bean `(requiredField1, requiredField2, requiredList)`.
- `IDConfigTest`: ID-field beans with the exact existing fields, empty entity-id marker data classes, and ID value `(value)`.

For every former `@FieldNameConstants` class add:

```kotlin
object Fields {
  const val propertyName = "propertyName"
}
```

for each property, so Java continues to use `Type.Fields.propertyName`. Keep the constants' spelling and visibility exactly as today.

Remove `import lombok.val` from `BaseValidationConfigTest` and replace its inferred locals with Java `final var`; do not add a replacement dependency.

- [ ] **Step 4: Run the converted-test groups**

```bash
./gradlew :vador:test \
  --tests 'com.salesforce.vador.execution.*' \
  --tests 'com.salesforce.vador.execution.spec.*' \
  --tests 'com.salesforce.vador.execution.config.*' \
  --tests 'com.salesforce.vador.lift.*' \
  --tests com.salesforce.vador.specs.factory.SpecFactoryTest \
  --no-daemon --console=plain
```

Expected: PASS. Kotlin fixtures must still expose JavaBean getters used by method references and runtime field annotations used by annotation validation.

- [ ] **Step 5: Commit the Kotlin test-fixture conversion**

```bash
git add vador/src/test
git commit -m "test: replace Lombok values with Kotlin fixtures"
```

---

### Task 8: Remove Lombok from intentional Java consumer fixtures

**Files:**

- Modify: `vador/src/test/java/sample/consumer/bean/Parent.java`
- Modify: `vador/src/test/java/sample/consumer/bean/Container.java`
- Modify: `vador/src/test/java/sample/consumer/bean/Member.java`
- Modify: `vador/src/test/java/sample/consumer/config/ConfigForValidators.java`
- Modify: `vador/src/test/java/sample/consumer/failure/ValidationFailure.java`
- Modify: `vador/src/test/java/sample/consumer/failure/ValidationFailureMessage.java`
- Modify: `vador/src/test/java/sample/consumer/validators/BeanValidator.java`
- Modify: `vador/src/test/java/com/salesforce/vador/specs/failure/ValidationFailure.java`
- Modify: `vador/src/test/java/com/salesforce/vador/specs/failure/ValidationFailureMessage.java`
- Modify: `vador/src/test/java/com/salesforce/vador/lift/InheritanceLiftEtrUtilKtTest.java`

These files intentionally remain Java so the test suite continues to exercise Java consumption.

- [ ] **Step 1: Replace constructor/getter/value annotations explicitly**

- `Parent`: keep the required three-argument constructor and the all-argument constructor; add JavaBean getters for all eight fields; implement value equality/hash code over all fields. Exact `toString()` formatting is not an API requirement.
- `Member`: add its one-argument constructor, `getId`, value equality/hash code, and informative `toString`.
- `Container`: remove the annotation and keep its three public constructors; provide an informative superclass-aware `toString`.
- `BeanValidator.Bean`: add its one-argument constructor and `getId`.
- `InheritanceLiftEtrUtilKtTest.Child`: replace Lombok equality/hash code with explicit class-based equality including the parent contract.

- [ ] **Step 2: Replace utility/mutable-data annotations explicitly**

- `ConfigForValidators`: remove `@UtilityClass`, retain static methods, and add a private constructor that throws `AssertionError`.
- Both `ValidationFailure` classes: retain existing required constructors, getters, mutable `exceptionMsg` setter, value equality/hash code, and static constants/factories.
- Both `ValidationFailureMessage` enums: retain constructors and getters; add explicit `setParams(Object[] params)` where Lombok supplied it.

Use `final var` for new Java locals, matching the repository's Java style.

- [ ] **Step 3: Prove the Java consumer suite remains source-compatible**

```bash
./gradlew :vador:test \
  --tests 'sample.consumer.*' \
  --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --tests com.salesforce.vador.lift.InheritanceLiftEtrUtilKtTest \
  --no-daemon --console=plain
```

If the test filter reports no tests for `sample.consumer.*` because those classes are fixtures rather than tests, require `:vador:compileTestJava` and the Java DSL compatibility test to pass instead.

- [ ] **Step 4: Commit the Java consumer-fixture cleanup**

```bash
git add vador/src/test/java
git commit -m "test: remove Lombok from Java consumer fixtures"
```

---

### Task 9: Remove Lombok/delombok and configure generated artifacts

**Files:**

- Modify: `build.gradle.kts`
- Modify: `vador/build.gradle.kts`
- Modify: `libs.versions.toml`
- Modify: `CONTRIBUTING.adoc`
- Delete: `lombok.config`

- [ ] **Step 1: Demonstrate that removal is not complete yet**

Run:

```bash
git grep -I -i lombok -- \
  ':!docs/superpowers/specs/2026-08-31-lombok-to-immutables-kotlin-design.md' \
  ':!docs/superpowers/plans/2026-08-31-lombok-to-immutables-kotlin.md'
```

Expected: FAIL the zero-match gate by printing the remaining Gradle, catalog, documentation, and `lombok.config` references.

- [ ] **Step 2: Remove the root Lombok/Sonar workaround**

From `build.gradle.kts`, remove:

- the `LOMBOK_VERSION` import;
- `alias(libs.plugins.lombok.gradle) apply false`;
- the `lombokForSonarQube` configuration and dependency;
- the Lombok-derived `sonar.java.libraries` property.

Keep all unrelated Sonar, Detekt, Kover, Nexus, and publishing configuration unchanged.

- [ ] **Step 3: Remove the module delombok pipeline**

From `vador/build.gradle.kts`, remove:

- `alias(libs.plugins.lombok.gradle)`;
- the conditional Kotlin source-set replacement;
- the `delombok` task block.

Keep KAPT and the standard `src/main/kotlin` layout.

- [ ] **Step 4: Add generated-source publication and coverage filtering**

In `vador/build.gradle.kts`, configure:

```kotlin
val generatedKaptMain = layout.buildDirectory.dir("generated/source/kapt/main")

tasks.named<Jar>("sourcesJar") {
  dependsOn(tasks.named("kaptKotlin"))
  from(generatedKaptMain)
}

tasks.named<Javadoc>("javadoc") {
  dependsOn(tasks.named("kaptKotlin"))
  source(generatedKaptMain)
}
```

Add required `Jar` and `Javadoc` imports. In the root Kover configuration, retain the existing total HTML report and add:

```kotlin
reports {
  filters {
    excludes { annotatedBy("org.immutables.value.Generated") }
  }
}
```

Generated Java is under `build`, so existing Spotless/Detekt/Sonar source targeting must not be broadened to analyze it.

- [ ] **Step 5: Remove catalog/config/documentation remnants**

- Remove `lombok-gradle = "8.12.1"` and the `lombok-gradle` plugin alias from `libs.versions.toml`.
- Delete `lombok.config`.
- Replace the delombok warning and Lombok IDE-plugin paragraph in `CONTRIBUTING.adoc` with a short statement that the build uses standard Kotlin sources and generated Java is produced automatically by Gradle. Update “mix of Java and Kotlin” to state that production source is Kotlin while Java consumers remain supported.

- [ ] **Step 6: Run the zero-match and source-layout gates**

```bash
git grep -I -i lombok -- \
  ':!docs/superpowers/specs/2026-08-31-lombok-to-immutables-kotlin-design.md' \
  ':!docs/superpowers/plans/2026-08-31-lombok-to-immutables-kotlin.md'
find vador/src/main -type f -name '*.java' -print
```

Expected: both commands produce no output. The plan is excluded only while it is an active implementation instruction; after implementation, the final operational policy should retain only the approved design record as the historical exception.

- [ ] **Step 7: Commit complete Lombok removal**

```bash
git add build.gradle.kts vador/build.gradle.kts libs.versions.toml CONTRIBUTING.adoc lombok.config
git commit -m "build: remove Lombok and delombok"
```

---

### Task 10: Verify generated publication artifacts and dependency hygiene

**Files:**

- Verify: `vador/build/libs/vador-1.1.1-SNAPSHOT.jar`
- Verify: `vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar`
- Verify: `vador/build/libs/vador-1.1.1-SNAPSHOT-javadoc.jar`
- Verify: `vador/build/publications/vador/pom-default.xml`

- [ ] **Step 1: Build all publication artifacts from clean state**

```bash
./gradlew clean \
  :vador:jar \
  :vador:sourcesJar \
  :vador:javadocJar \
  :vador:generatePomFileForVadorPublication \
  --no-daemon --console=plain
```

Expected: PASS.

- [ ] **Step 2: Assert generated public sources and docs are included**

```bash
jar tf vador/build/libs/vador-1.1.1-SNAPSHOT-sources.jar | rg \
  'com/salesforce/vador/(config/ValidationConfig|specs/specs/Spec1)\.java'
jar tf vador/build/libs/vador-1.1.1-SNAPSHOT-javadoc.jar | rg \
  'com/salesforce/vador/(config/ValidationConfig|specs/specs/Spec1)\.html'
```

Expected: both generated public types appear in both artifact listings.

- [ ] **Step 3: Assert runtime dependency hygiene**

```bash
./gradlew :vador:dependencies --configuration runtimeClasspath --no-daemon --console=plain
rg -n -i 'immutables|lombok' vador/build/publications/vador/pom-default.xml
```

Expected: Immutables and Lombok are absent from runtimeClasspath and the published POM; the final `rg` command returns no matches.

- [ ] **Step 4: Assert the generated public API directly**

```bash
javap -classpath vador/build/classes/kotlin/main:vador/build/classes/java/main -public \
  com.salesforce.vador.config.ValidationConfig \
  'com.salesforce.vador.config.ValidationConfig$Builder' \
  com.salesforce.vador.config.BatchValidationConfig \
  com.salesforce.vador.specs.specs.Spec1 \
  'com.salesforce.vador.specs.specs.Spec1$Builder'
```

Expected: public entry points, terminal methods, getters, `toBuilder`, and inherited base assignability are present. Do not compare Lombok builder class names or exact `toString()` output.

- [ ] **Step 5: Commit artifact-wiring corrections only if verification required changes**

```bash
git status --short
```

If clean, do not create an empty commit. If Task 10 exposed and fixed artifact wiring, stage only those fixes, rerun Steps 1–4, and commit them as:

```bash
git commit -m "build: publish generated Immutables API sources"
```

---

### Task 11: Run the complete verification matrix and finish cleanly

**Files:**

- Verify the complete repository and worktree

- [ ] **Step 1: Run formatting and the complete test/check lifecycle**

```bash
./gradlew spotlessApply --no-daemon --console=plain
./gradlew clean :vador:test check --no-daemon --console=plain
```

Expected: PASS. If `spotlessApply` changes files, inspect and commit only migration-owned formatting changes before continuing.

- [ ] **Step 2: Re-run from configuration cache**

```bash
./gradlew :vador:test check --configuration-cache --no-daemon --console=plain
./gradlew :vador:test check --configuration-cache --no-daemon --console=plain
```

Expected: both PASS; the second run reports configuration-cache reuse. Accepted KAPT warnings are not failures.

- [ ] **Step 3: Run focused Java and Kotlin code-generation gates**

```bash
./gradlew :vador:compileKotlin :vador:compileJava :vador:compileTestKotlin :vador:compileTestJava \
  --no-daemon --console=plain
./gradlew :vador:test --tests com.salesforce.vador.compatibility.JavaDslCompatibilityTest \
  --no-daemon --console=plain
```

Expected: PASS.

- [ ] **Step 4: Run preliminary structural hygiene checks**

```bash
test -z "$(find vador/src/main -type f -name '*.java' -print -quit)"
test -z "$(find vador/src/main/java -type f -print -quit 2>/dev/null)"
git grep -I -i lombok -- \
  ':!docs/superpowers/specs/2026-08-31-lombok-to-immutables-kotlin-design.md' \
  ':!docs/superpowers/plans/2026-08-31-lombok-to-immutables-kotlin.md'
git grep -I -n -E 'delombok|io\.freefair\.lombok|org\.projectlombok' -- \
  ':!docs/superpowers/specs/2026-08-31-lombok-to-immutables-kotlin-design.md' \
  ':!docs/superpowers/plans/2026-08-31-lombok-to-immutables-kotlin.md'
```

Expected: no handwritten production Java and no operational Lombok/delombok matches. The plan exclusion is temporary task documentation; archive or remove it after execution if the repository requires the design spec to be the literal sole historical match.

- [ ] **Step 5: Inspect the final diff and commits**

```bash
git diff --check
git status --short
git log --oneline --decorate -12
```

Expected: `git diff --check` passes and the worktree is clean. The log shows reviewable commits for the compatibility baseline, codegen foundation, spec family, config families, test fixtures, and build cleanup.

- [ ] **Step 6: Request final code review**

Use `superpowers:requesting-code-review` against the fixed pre-migration commit. Require the review to check both the approved design and this plan, with special attention to Java source compatibility, all singular aliases, nullability, generated artifact contents, and accidental Lombok remnants.

- [ ] **Step 7: Apply review feedback rigorously and reverify**

Use `superpowers:receiving-code-review` for any feedback. After every accepted change, rerun the smallest focused gate plus Steps 1–5 before claiming completion.

- [ ] **Step 8: Retire this executed plan and run the literal final zero-match gate**

After review feedback is resolved and all earlier gates are green, remove this implementation plan so the approved design record remains the sole historical Lombok reference, as required by the spec. Then run:

```bash
git add docs/superpowers/plans/2026-08-31-lombok-to-immutables-kotlin.md
git commit -m "docs: retire the executed Lombok migration plan"
git grep -I -i lombok -- \
  ':!docs/superpowers/specs/2026-08-31-lombok-to-immutables-kotlin-design.md'
git status --short
```

Expected: the grep prints nothing and exits with status 1 because there are no matches outside the approved design record; `git status --short` prints nothing.
