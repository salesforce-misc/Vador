/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.compatibility

import com.salesforce.vador.config.BatchOfBatch1ValidationConfig
import com.salesforce.vador.config.BatchValidationConfig
import com.salesforce.vador.config.FieldConfig
import com.salesforce.vador.config.FieldConfigBuilder
import com.salesforce.vador.config.FilterDuplicatesConfig
import com.salesforce.vador.config.FilterDuplicatesConfigBuilder
import com.salesforce.vador.config.IDConfig
import com.salesforce.vador.config.IDConfigBuilder
import com.salesforce.vador.config.ValidationConfig
import com.salesforce.vador.config.base.BaseFilterDuplicatesConfig
import com.salesforce.vador.config.container.ContainerValidationConfig
import com.salesforce.vador.specs.specs.Spec1
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import org.hamcrest.Matchers.equalTo

class KotlinDslCompatibilityTest :
  FunSpec({
    test("generated values retain the Kotlin DSL and independent copies") {
      val spec =
        Spec1.check<Bean, String, String>()
          .given(Function1 { it.text })
          .shouldMatch(equalTo("valid"))
          .orFailWith("bad-spec")
          .done()
      val textGetter = TypedPropertyGetter<Bean, String> { it.text }
      val numberGetter = TypedPropertyGetter<Bean, Int> { it.number }
      val validationConfig =
        ValidationConfig.toValidate<Bean, String>()
          .shouldHaveFieldOrFailWith(textGetter, "required")
          .withSpec { factory ->
            factory
              ._1<String>()
              .given(Function1 { it.text })
              .shouldMatch(equalTo("valid"))
              .orFailWith("bad-spec")
          }
          .prepare()

      spec.given.apply(Bean("valid", 1)) shouldBe "valid"
      spec.shouldMatchAnyOf.size shouldBe 1
      validationConfig.shouldHaveFieldsOrFailWith.size shouldBe 1
      validationConfig.withSpecs.size shouldBe 1

      val copied =
        validationConfig
          .toBuilder()
          .shouldHaveFieldOrFailWith(numberGetter, "number-required")
          .prepare()

      validationConfig.shouldHaveFieldsOrFailWith.size shouldBe 1
      copied.shouldHaveFieldsOrFailWith.size shouldBe 2
      validationConfig.shouldHaveFieldsOrFailWith[numberGetter] shouldBe null
      copied.shouldHaveFieldsOrFailWith[numberGetter] shouldBe "number-required"
      (copied === validationConfig) shouldBe false
    }

    test("generated child builders stay lazy through ConfigBuilder") {
      val idBuilder = IDConfig.toValidate<String, Bean, String, String>()
      val fieldBuilder = FieldConfig.toValidate<String, Bean, String>()
      val duplicatesBuilder = FilterDuplicatesConfig.toValidate<Bean, String>()
      val validationConfig =
        ValidationConfig.toValidate<Bean, String>()
          .withIdConfig(idBuilder)
          .withFieldConfig(fieldBuilder)
          .prepare()
      val batchConfig =
        BatchValidationConfig.toValidate<Bean, String>()
          .findAndFilterDuplicatesConfig(duplicatesBuilder)
          .prepare()

      val storedIdBuilder: IDConfigBuilder<*, Bean, String, *> =
        validationConfig.withIdConfigs.single()
      val storedFieldBuilder: FieldConfigBuilder<*, Bean, String> =
        validationConfig.withFieldConfigs.single()
      val storedDuplicatesBuilder: FilterDuplicatesConfigBuilder<Bean, String?> =
        batchConfig.findAndFilterDuplicatesConfigs.single()
      val preparedDuplicates: BaseFilterDuplicatesConfig<Bean, String?> =
        storedDuplicatesBuilder.prepare()

      (storedIdBuilder === idBuilder) shouldBe true
      (storedIdBuilder.prepare() is IDConfig<*, *, *, *>) shouldBe true
      (storedFieldBuilder === fieldBuilder) shouldBe true
      (storedFieldBuilder.prepare() is FieldConfig<*, *, *>) shouldBe true
      (storedDuplicatesBuilder === duplicatesBuilder) shouldBe true
      (preparedDuplicates is FilterDuplicatesConfig<*, *>) shouldBe true
    }

    test("validation DSL accepts natural nonnullable validator types") {
      val validator = Validator<Bean, String> { "bad-validator" }
      val validatorEtr = ValidatorEtr<Bean, String> { it }
      val nullableFailureValidator = Validator<Bean, String?> { null }
      val validatorChain: Tuple2<Collection<Validator<in Bean, String?>>, String> =
        Tuple.of(listOf(nullableFailureValidator), "none")
      val annotationFailures = Tuple.of(mapOf<String, String?>("required" to null), null as String?)
      val config =
        ValidationConfig.toValidate<Bean, String>()
          .withValidator(validator, "bad-validator")
          .withValidatorEtr(validatorEtr)
          .withValidators(validatorChain)
          .forAnnotations(annotationFailures)
          .prepare()

      config.withValidator[validator] shouldBe "bad-validator"
      config.withValidatorEtrs.single() shouldBe validatorEtr
      config.withValidators shouldBe validatorChain
      config.withValidators?._2 shouldBe "none"
      config.forAnnotations shouldBe annotationFailures
      config.forAnnotations?._1?.get("required") shouldBe null
    }

    test("container DSL preserves its validator and tuple nullability") {
      val validator = Validator<Container, String> { "bad-validator" }
      val validatorEtr = ValidatorEtr<Container, String> { it }
      val nullableFailureValidator = Validator<Container, String?> { null }
      val validatorChain: Tuple2<Collection<Validator<in Container, String?>>, String?> =
        Tuple.of(listOf(nullableFailureValidator), null as String?)
      val batchMember = TypedPropertyGetter<Container, Collection<*>?> { it.beans }
      val config =
        ContainerValidationConfig.toValidate<Container, String>()
          .withContainerValidator(validator, "bad-validator")
          .withContainerValidatorEtr(validatorEtr)
          .withContainerValidators(validatorChain)
          .withBatchMember(batchMember)
          .prepare()

      val validatorEtrs: Collection<ValidatorEtr<Container, String>> =
        config.withContainerValidatorEtrs
      val validators: Tuple2<Collection<Validator<in Container, String?>>, String?>? =
        config.withContainerValidators
      val validatorMap: Map<Validator<in Container, String>, String> = config.withContainerValidator
      val executionValidators: List<ValidatorEtr<Container, String?>> = config.containerValidators

      validatorEtrs.single() shouldBe validatorEtr
      validators shouldBe validatorChain
      validators?._2 shouldBe null
      validatorMap[validator] shouldBe "bad-validator"
      executionValidators.size shouldBe 3
      (executionValidators.last() === validatorEtr) shouldBe true
    }

    test("batch-of-batch stores its supplied nested value and copies independently") {
      val memberConfig = BatchValidationConfig.toValidate<Bean, String?>().prepare()
      val members = Function1<Container, Collection<Bean>> { it.beans }
      val config =
        BatchOfBatch1ValidationConfig.toValidate<Container, Bean, String>()
          .withMemberBatchValidationConfig(Tuple.of(members, memberConfig))
          .prepare()

      val copied =
        config
          .toBuilder()
          .shouldHaveFieldOrFailWith(TypedPropertyGetter { it.beans }, "required")
          .prepare()

      (config.withMemberBatchValidationConfig._2 === memberConfig) shouldBe true
      (copied.withMemberBatchValidationConfig._2 === memberConfig) shouldBe true
      config.shouldHaveFieldsOrFailWith.size shouldBe 0
      copied.shouldHaveFieldsOrFailWith.size shouldBe 1
      (copied === config) shouldBe false
    }
  })

private data class Bean(val text: String, val number: Int)

private data class Container(val beans: List<Bean>)
