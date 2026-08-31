/*******************************************************************************
 * Copyright (c) 2026, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.compatibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.equalTo;

import com.salesforce.vador.config.BatchOfBatch1ValidationConfig;
import com.salesforce.vador.config.BatchValidationConfig;
import com.salesforce.vador.config.ConfigBuilder;
import com.salesforce.vador.config.FieldConfig;
import com.salesforce.vador.config.FieldConfigBuilder;
import com.salesforce.vador.config.FilterDuplicatesConfig;
import com.salesforce.vador.config.FilterDuplicatesConfigBuilder;
import com.salesforce.vador.config.IDConfig;
import com.salesforce.vador.config.IDConfigBuilder;
import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.config.base.BaseBatchValidationConfig;
import com.salesforce.vador.config.base.BaseContainerValidationConfig;
import com.salesforce.vador.config.base.BaseFieldConfig;
import com.salesforce.vador.config.base.BaseFilterDuplicatesConfig;
import com.salesforce.vador.config.base.BaseIDConfig;
import com.salesforce.vador.config.base.BaseValidationConfig;
import com.salesforce.vador.config.container.ContainerValidationConfig;
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels;
import com.salesforce.vador.specs.factory.SpecFactory;
import com.salesforce.vador.specs.specs.Spec1;
import com.salesforce.vador.specs.specs.Spec2;
import com.salesforce.vador.specs.specs.Spec3;
import com.salesforce.vador.specs.specs.Spec4;
import com.salesforce.vador.specs.specs.base.BaseSpec;
import com.salesforce.vador.types.Spec;
import com.salesforce.vador.types.Validator;
import com.salesforce.vador.types.ValidatorEtr;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class JavaDslCompatibilityTest {

	@Test
	void representativeJavaDslChainsKeepTheirPublicValueContract() {
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
						.withSpec(
								factory ->
										factory
												.<String>_1()
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

		assertThat(fieldConfig.getWithFieldValidator().test("valid")).isTrue();
		assertThat(fieldConfig.getWithFieldValidator().test("")).isFalse();
		assertThat(fieldConfig.getShouldHaveValidFormatForAllOrFailWith())
				.hasSize(1)
				.containsValue("bad-format");
		assertThat(spec.getGiven().apply(new Bean("valid"))).isEqualTo("valid");
		assertThat(spec.getShouldMatchAnyOf()).hasSize(1);
		assertThat(spec.getOrFailWith()).isEqualTo("bad-spec");
		assertThat(validationConfig.getShouldHaveFieldsOrFailWith())
				.hasSize(1)
				.containsValue("required");
		assertThat(validationConfig.getWithSpecs()).hasSize(1);
		assertThat(batchConfig.getFindAndFilterDuplicatesConfigs()).hasSize(1);
		assertThat(containerConfig.getWithBatchMembers()).hasSize(1);

		assertThat(fieldConfig).isEqualTo(fieldConfig.toBuilder().prepare());
		assertThat(fieldConfig.hashCode()).isEqualTo(fieldConfig.toBuilder().prepare().hashCode());
		assertThat(fieldConfig.toString()).isNotBlank().contains("FieldConfig");
		assertThat(spec.toString()).isNotBlank().contains("Spec1");
		assertThat(validationConfig.toString()).isNotBlank().contains("ValidationConfig");
		assertThat(batchConfig.toString()).isNotBlank().contains("BatchValidationConfig");
		assertThat(containerConfig.toString()).isNotBlank().contains("ContainerValidationConfig");
		assertThat(spec).isInstanceOf(BaseSpec.class);
		assertThat(validationConfig).isInstanceOf(BaseValidationConfig.class);
		assertThat(batchConfig).isInstanceOf(BaseBatchValidationConfig.class);
		assertThat(containerConfig).isInstanceOf(BaseContainerValidationConfig.class);
	}

	@Test
	void fieldConfigToBuilderCopiesWithoutMutatingTheOriginal() {
		final var fieldConfig =
				FieldConfig.<String, Bean, String>toValidate()
						.shouldHaveValidFormatOrFailWith(Bean::getText, "bad-format")
						.prepare();
		final var copied =
				fieldConfig.toBuilder()
						.absentOrHaveValidFormatOrFailWith(Bean::getText, "optional-format")
						.prepare();

		assertThat(fieldConfig.getAbsentOrHaveValidFormatForAllOrFailWith()).isEmpty();
		assertThat(copied.getAbsentOrHaveValidFormatForAllOrFailWith())
				.hasSize(1)
				.containsValue("optional-format");
		assertThat(copied).isNotEqualTo(fieldConfig);
		assertThat(copied.toString()).isNotBlank().contains("FieldConfig");
	}

	@Test
	void generatedChildBuildersStayLazyThroughThePublicConfigBuilderProtocol() {
		final var idBuilder = IDConfig.<String, Bean, String, String>toValidate();
		final var fieldBuilder = FieldConfig.<String, Bean, String>toValidate();
		final var duplicatesBuilder = FilterDuplicatesConfig.<Bean, String>toValidate();
		final var validationConfig =
				ValidationConfig.<Bean, String>toValidate()
						.withIdConfig(idBuilder)
						.withFieldConfig(fieldBuilder)
						.prepare();
		final var batchConfig =
				BatchValidationConfig.<Bean, String>toValidate()
						.findAndFilterDuplicatesConfig(duplicatesBuilder)
						.prepare();

		final IDConfigBuilder<?, Bean, String, ?> storedIdBuilder =
				validationConfig.getWithIdConfigs().iterator().next();
		final FieldConfigBuilder<?, Bean, String> storedFieldBuilder =
				validationConfig.getWithFieldConfigs().iterator().next();
		final FilterDuplicatesConfigBuilder<Bean, String> storedDuplicatesBuilder =
				batchConfig.getFindAndFilterDuplicatesConfigs().iterator().next();
		final BaseIDConfig<?, Bean, String, ?> preparedId = storedIdBuilder.prepare();
		final BaseFieldConfig<?, Bean, String> preparedField = storedFieldBuilder.prepare();
		final BaseFilterDuplicatesConfig<Bean, String> preparedDuplicates =
				storedDuplicatesBuilder.prepare();

		assertThat(storedIdBuilder).isSameAs(idBuilder);
		assertThat(preparedId).isInstanceOf(IDConfig.class);
		assertThat(storedFieldBuilder).isSameAs(fieldBuilder);
		assertThat(preparedField).isInstanceOf(FieldConfig.class);
		assertThat(storedDuplicatesBuilder).isSameAs(duplicatesBuilder);
		assertThat(preparedDuplicates).isInstanceOf(FilterDuplicatesConfig.class);
	}

	@Test
	void batchOfBatchStoresItsSuppliedNestedValueAndCopiesIndependently() {
		final var memberConfig = BatchValidationConfig.<Bean, String>toValidate().prepare();
		final Function1<Container, java.util.Collection<Bean>> members = Container::getBeans;
		final var config =
				BatchOfBatch1ValidationConfig.<Container, Bean, String>toValidate()
						.withMemberBatchValidationConfig(Tuple.of(members, memberConfig))
						.prepare();

		final var copied =
				config.toBuilder().shouldHaveFieldOrFailWith(Container::getBeans, "required").prepare();

		assertThat(config.getWithMemberBatchValidationConfig()._2).isSameAs(memberConfig);
		assertThat(copied.getWithMemberBatchValidationConfig()._2).isSameAs(memberConfig);
		assertThat(config.getShouldHaveFieldsOrFailWith()).isEmpty();
		assertThat(copied.getShouldHaveFieldsOrFailWith()).hasSize(1);
		assertThat(copied).isNotEqualTo(config);
	}

	@Test
	void childBuilderMethodsExposeSealedCategorySpecificTypedProtocols() {
		assertBuilderMethodParameter(
				ValidationConfig.toValidate(), "withIdConfig", IDConfigBuilder.class);
		assertBuilderMethodParameter(
				ValidationConfig.toValidate(), "withFieldConfig", FieldConfigBuilder.class);
		assertBuilderMethodParameter(
				BatchValidationConfig.toValidate(),
				"findAndFilterDuplicatesConfig",
				FilterDuplicatesConfigBuilder.class);

		assertCategoryProtocolResult(IDConfigBuilder.class, "BaseIDConfig");
		assertCategoryProtocolResult(FieldConfigBuilder.class, "BaseFieldConfig");
		assertCategoryProtocolResult(FilterDuplicatesConfigBuilder.class, "BaseFilterDuplicatesConfig");
		assertThat(
						Stream.of(BatchOfBatch1ValidationConfig.toValidate().getClass().getMethods())
								.noneMatch(
										method -> method.getName().equals("withMemberBatchValidationConfigBuilder")))
				.isTrue();
		assertThat(IDConfigBuilder.class.isSealed()).isTrue();
		assertThat(FieldConfigBuilder.class.isSealed()).isTrue();
		assertThat(FilterDuplicatesConfigBuilder.class.isSealed()).isTrue();
	}

	@Test
	void spec1RequiresGivenBeforeItCanBeBuilt() {
		assertThatThrownBy(() -> Spec1.check().orFailWith("failure").done())
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	void baseSpecFailureConfigurationMessageRemainsAJavaCompileTimeConstant() {
		assertThat(isFailureConfigurationMessage(BaseSpec.INVALID_FAILURE_CONFIG)).isTrue();
		assertThat(BaseSpec.INVALID_FAILURE_CONFIG)
				.isEqualTo(
						"For Spec with: %s Either 'orFailWith' or 'orFailWithFn' should be passed, but not both");
	}

	@Test
	void spec2ExplicitTypeWitnessBulkRelationsAccumulate() {
		final var spec =
				new SpecFactory<Bean, String>()
						.<String, String>_2()
						.when(Bean::getText)
						.then(Bean::getText)
						.shouldRelateWith(Map.of("first", Set.of("one")))
						.shouldRelateWith(Map.of("second", Set.of("two")))
						.orFailWith("failure")
						.done();

		assertThat(spec.getShouldRelateWith())
				.hasSize(2)
				.isEqualTo(Map.of("first", Set.of("one"), "second", Set.of("two")));
	}

	@Test
	void spec2NoTypeWitnessBulkRelationsAccumulate() {
		final var spec =
				new SpecFactory<Bean, String>()
						._2()
						.when(Bean::getText)
						.then(Bean::getText)
						.shouldRelateWith(Map.of("first", Set.of("one")))
						.shouldRelateWith(Map.of("second", Set.of("two")))
						.orFailWith("failure")
						.done();

		assertThat(spec.getShouldRelateWith())
				.hasSize(2)
				.isEqualTo(Map.of("first", Set.of("one"), "second", Set.of("two")));
	}

	@Test
	void spec3ExplicitTypeWitnessBulkRelationsAccumulate() {
		final var spec =
				new SpecFactory<Bean, String>()
						.<String, String, String>_3()
						.when(Bean::getText)
						.thenField1(Bean::getText)
						.thenField2(Bean::getText)
						.shouldRelateWith(Map.of("first", Set.of("one")))
						.shouldRelateWith(Map.of("second", Set.of("two")))
						.orFailWith("failure")
						.done();

		assertThat(spec.getShouldRelateWith())
				.hasSize(2)
				.isEqualTo(Map.of("first", Set.of("one"), "second", Set.of("two")));
	}

	@Test
	void spec3NoTypeWitnessBulkRelationsAccumulate() {
		final var spec =
				new SpecFactory<Bean, String>()
						._3()
						.when(Bean::getText)
						.thenField1(Bean::getText)
						.thenField2(Bean::getText)
						.shouldRelateWith(Map.of("first", Set.of("one")))
						.shouldRelateWith(Map.of("second", Set.of("two")))
						.orFailWith("failure")
						.done();

		assertThat(spec.getShouldRelateWith())
				.hasSize(2)
				.isEqualTo(Map.of("first", Set.of("one"), "second", Set.of("two")));
	}

	@Test
	void fieldConfigConsecutiveBulkMapsAccumulate() {
		final TypedPropertyGetter<Bean, String> firstField = Bean::getText;
		final TypedPropertyGetter<Bean, String> secondField = bean -> bean.getText();
		final Function2<String, String, String> firstFailure = (name, value) -> "first";
		final Function2<String, String, String> secondFailure = (name, value) -> "second";

		final var fieldConfig =
				FieldConfig.<String, Bean, String>toValidate()
						.shouldHaveValidFormatForAllOrFailWith(Map.of(firstField, "first"))
						.shouldHaveValidFormatForAllOrFailWith(Map.of(secondField, "second"))
						.shouldHaveValidFormatOrFailWithFn(Map.of(firstField, firstFailure))
						.shouldHaveValidFormatOrFailWithFn(Map.of(secondField, secondFailure))
						.absentOrHaveValidFormatForAllOrFailWith(Map.of(firstField, "first"))
						.absentOrHaveValidFormatForAllOrFailWith(Map.of(secondField, "second"))
						.absentOrHaveValidFormatOrFailWithFn(Map.of(firstField, firstFailure))
						.absentOrHaveValidFormatOrFailWithFn(Map.of(secondField, secondFailure))
						.prepare();

		assertThat(fieldConfig.getShouldHaveValidFormatForAllOrFailWith()).hasSize(2);
		assertThat(fieldConfig.getShouldHaveValidFormatOrFailWithFn()).hasSize(2);
		assertThat(fieldConfig.getAbsentOrHaveValidFormatForAllOrFailWith()).hasSize(2);
		assertThat(fieldConfig.getAbsentOrHaveValidFormatOrFailWithFn()).hasSize(2);
	}

	@Test
	void idConfigConsecutiveBulkMapsAccumulate() {
		final TypedPropertyGetter<Bean, String> firstField = Bean::getText;
		final TypedPropertyGetter<Bean, String> secondField = bean -> bean.getText();
		final Tuple2<TypedPropertyGetter<Bean, String>, String> firstId =
				Tuple.of(firstField, "first-entity");
		final Tuple2<TypedPropertyGetter<Bean, String>, String> secondId =
				Tuple.of(secondField, "second-entity");
		final Tuple2<TypedPropertyGetter<Bean, String>, List<String>> firstPolymorphicId =
				Tuple.of(firstField, List.of("first-entity"));
		final Tuple2<TypedPropertyGetter<Bean, String>, List<String>> secondPolymorphicId =
				Tuple.of(secondField, List.of("second-entity"));
		final Function2<String, String, String> firstFailure = (name, value) -> "first";
		final Function2<String, String, String> secondFailure = (name, value) -> "second";

		final var idConfig =
				IDConfig.<String, Bean, String, String>toValidate()
						.shouldHaveValidSFIdFormatForAllOrFailWith(Map.of(firstId, "first"))
						.shouldHaveValidSFIdFormatForAllOrFailWith(Map.of(secondId, "second"))
						.shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(
								Map.of(firstPolymorphicId, "first"))
						.shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(
								Map.of(secondPolymorphicId, "second"))
						.shouldHaveValidSFIdFormatOrFailWithFn(Map.of(firstId, firstFailure))
						.shouldHaveValidSFIdFormatOrFailWithFn(Map.of(secondId, secondFailure))
						.shouldHaveValidSFPolymorphicIdFormatOrFailWithFn(
								Map.of(firstPolymorphicId, firstFailure))
						.shouldHaveValidSFPolymorphicIdFormatOrFailWithFn(
								Map.of(secondPolymorphicId, secondFailure))
						.absentOrHaveValidSFIdFormatForAllOrFailWith(Map.of(firstId, "first"))
						.absentOrHaveValidSFIdFormatForAllOrFailWith(Map.of(secondId, "second"))
						.absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(
								Map.of(firstPolymorphicId, "first"))
						.absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(
								Map.of(secondPolymorphicId, "second"))
						.absentOrHaveValidSFIdFormatOrFailWithFn(Map.of(firstId, firstFailure))
						.absentOrHaveValidSFIdFormatOrFailWithFn(Map.of(secondId, secondFailure))
						.absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn(
								Map.of(firstPolymorphicId, firstFailure))
						.absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn(
								Map.of(secondPolymorphicId, secondFailure))
						.prepare();

		assertThat(idConfig.getShouldHaveValidSFIdFormatForAllOrFailWith()).hasSize(2);
		assertThat(idConfig.getShouldHaveValidSFPolymorphicIdFormatForAllOrFailWith()).hasSize(2);
		assertThat(idConfig.getShouldHaveValidSFIdFormatOrFailWithFn()).hasSize(2);
		assertThat(idConfig.getShouldHaveValidSFPolymorphicIdFormatOrFailWithFn()).hasSize(2);
		assertThat(idConfig.getAbsentOrHaveValidSFIdFormatForAllOrFailWith()).hasSize(2);
		assertThat(idConfig.getAbsentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith()).hasSize(2);
		assertThat(idConfig.getAbsentOrHaveValidSFIdFormatOrFailWithFn()).hasSize(2);
		assertThat(idConfig.getAbsentOrHaveValidSFPolymorphicIdFormatOrFailWithFn()).hasSize(2);
	}

	@Test
	void inheritedConfigConsecutiveBulkSingularInputsAccumulate() {
		final TypedPropertyGetter<Bean, String> firstField = Bean::getText;
		final TypedPropertyGetter<Bean, String> secondField = bean -> bean.getText();
		final Function2<String, Object, String> firstFailure = (name, value) -> "first";
		final Function2<String, Object, String> secondFailure = (name, value) -> "second";
		final Validator<Bean, String> firstValidator = bean -> "first";
		final Validator<Bean, String> secondValidator = bean -> "second";
		final ValidatorEtr<Bean, String> firstValidatorEtr = value -> value;
		final ValidatorEtr<Bean, String> secondValidatorEtr = value -> value;
		final Spec<Bean, String> firstSpec =
				factory -> factory.<String>_1().given(Bean::getText).orFailWith("first");
		final Spec<Bean, String> secondSpec =
				factory -> factory.<String>_1().given(Bean::getText).orFailWith("second");

		final var validationConfig =
				ValidationConfig.<Bean, String>toValidate()
						.shouldHaveFieldsOrFailWith(Map.of(firstField, "first"))
						.shouldHaveFieldsOrFailWith(Map.of(secondField, "second"))
						.shouldHaveFieldOrFailWithFn(Map.of(firstField, firstFailure))
						.shouldHaveFieldOrFailWithFn(Map.of(secondField, secondFailure))
						.withIdConfigs(List.of(IDConfig.<String, Bean, String, String>toValidate()))
						.withIdConfigs(List.of(IDConfig.<String, Bean, String, String>toValidate()))
						.withFieldConfigs(List.of(FieldConfig.<String, Bean, String>toValidate()))
						.withFieldConfigs(List.of(FieldConfig.<String, Bean, String>toValidate()))
						.withSpecs(List.of(firstSpec))
						.withSpecs(List.of(secondSpec))
						.withValidatorEtrs(List.of(firstValidatorEtr))
						.withValidatorEtrs(List.of(secondValidatorEtr))
						.withValidator(Map.of(firstValidator, "first"))
						.withValidator(Map.of(secondValidator, "second"))
						.prepare();
		final var batchConfig =
				BatchValidationConfig.<Bean, String>toValidate()
						.findAndFilterDuplicatesConfigs(
								List.of(FilterDuplicatesConfig.<Bean, String>toValidate()))
						.findAndFilterDuplicatesConfigs(
								List.of(FilterDuplicatesConfig.<Bean, String>toValidate()))
						.prepare();

		assertThat(validationConfig.getShouldHaveFieldsOrFailWith()).hasSize(2);
		assertThat(validationConfig.getShouldHaveFieldOrFailWithFn()).hasSize(2);
		assertThat(validationConfig.getWithIdConfigs()).hasSize(2);
		assertThat(validationConfig.getWithFieldConfigs()).hasSize(2);
		assertThat(validationConfig.getWithSpecs()).hasSize(2);
		assertThat(validationConfig.getWithValidatorEtrs()).hasSize(2);
		assertThat(validationConfig.getWithValidator()).hasSize(2);
		assertThat(batchConfig.getFindAndFilterDuplicatesConfigs()).hasSize(2);
	}

	@Test
	void concreteBuildersExposeEverySupportedSingularJavaDslMethod() {
		assertBuilderHasSingularMethods(
				FieldConfig.toValidate(),
				"shouldHaveValidFormatOrFailWith",
				"shouldHaveValidFormatOrFailWithFn",
				"absentOrHaveValidFormatOrFailWith",
				"absentOrHaveValidFormatOrFailWithFn");
		assertBuilderHasSingularMethods(
				IDConfig.toValidate(),
				"shouldHaveValidSFIdFormatOrFailWith",
				"shouldHaveValidSFPolymorphicIdFormatOrFailWith",
				"shouldHaveValidSFIdFormatOrFailWithFn",
				"shouldHaveValidSFPolymorphicIdFormatOrFailWithFn",
				"absentOrHaveValidSFIdFormatOrFailWith",
				"absentOrHaveValidSFPolymorphicIdFormatOrFailWith",
				"absentOrHaveValidSFIdFormatOrFailWithFn",
				"absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn");
		assertBuilderHasSingularMethods(
				ValidationConfig.toValidate(),
				"shouldHaveFieldOrFailWith",
				"shouldHaveFieldOrFailWithFn",
				"withIdConfig",
				"withFieldConfig",
				"withSpec",
				"withValidatorEtr",
				"withValidator");
		assertBuilderHasSingularMethods(
				BatchValidationConfig.toValidate(), "findAndFilterDuplicatesConfig");
		assertBuilderHasSingularMethods(
				ContainerValidationConfig.toValidate(),
				"withContainerValidatorEtr",
				"withContainerValidator",
				"withBatchMember");
		assertBuilderHasSingularMethods(
				ContainerValidationConfigWith2Levels.toValidate(), "withBatchMember");
		assertBuilderHasSingularMethods(Spec1.check(), "shouldMatchField", "shouldMatch");
		assertBuilderHasSingularMethods(
				Spec2.check(), "matches", "shouldMatch", "shouldRelateWithEntry");
		assertBuilderHasSingularMethods(
				Spec3.check(),
				"matches",
				"shouldRelateWithEntry",
				"orField1ShouldMatch",
				"orField2ShouldMatch");
		assertBuilderHasSingularMethods(Spec4.check(), "whenFieldMatches", "thenFieldShouldMatch");
	}

	private static void assertBuilderHasSingularMethods(
			Object builder, String... expectedMethodNames) {
		final var methodNames =
				Stream.of(builder.getClass().getMethods()).map(Method::getName).toList();

		assertThat(methodNames).contains(expectedMethodNames);
	}

	private static void assertBuilderMethodParameter(
			Object builder, String methodName, Class<?> expectedParameterType) {
		final var method =
				Stream.of(builder.getClass().getMethods())
						.filter(candidate -> candidate.getName().equals(methodName))
						.filter(candidate -> candidate.getParameterCount() == 1)
						.findFirst()
						.orElseThrow();

		assertThat(method.getParameterTypes()).containsExactly(expectedParameterType);
	}

	private static void assertCategoryProtocolResult(
			Class<?> protocol, String expectedBaseSimpleName) {
		final var configBuilderType =
				Stream.of(protocol.getGenericInterfaces())
						.map(java.lang.reflect.Type::getTypeName)
						.filter(typeName -> typeName.startsWith(ConfigBuilder.class.getName()))
						.findFirst()
						.orElseThrow();

		assertThat(configBuilderType)
				.contains("com.salesforce.vador.config.base." + expectedBaseSimpleName + "<")
				.doesNotContain("java.lang.Object");
	}

	private static boolean isFailureConfigurationMessage(String value) {
		switch (value) {
			case BaseSpec.INVALID_FAILURE_CONFIG:
				return true;
			default:
				return false;
		}
	}

	private static final class Bean {
		private final String text;

		private Bean(String text) {
			this.text = text;
		}

		String getText() {
			return text;
		}
	}

	private static final class Container {
		private final List<Bean> beans;

		private Container(List<Bean> beans) {
			this.beans = beans;
		}

		List<Bean> getBeans() {
			return beans;
		}
	}
}
