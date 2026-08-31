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

import com.salesforce.vador.config.BatchValidationConfig;
import com.salesforce.vador.config.FieldConfig;
import com.salesforce.vador.config.FilterDuplicatesConfig;
import com.salesforce.vador.config.IDConfig;
import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.config.base.BaseBatchValidationConfig;
import com.salesforce.vador.config.base.BaseContainerValidationConfig;
import com.salesforce.vador.config.base.BaseValidationConfig;
import com.salesforce.vador.config.container.ContainerValidationConfig;
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels;
import com.salesforce.vador.specs.specs.Spec1;
import com.salesforce.vador.specs.specs.Spec2;
import com.salesforce.vador.specs.specs.Spec3;
import com.salesforce.vador.specs.specs.Spec4;
import com.salesforce.vador.specs.specs.base.BaseSpec;
import java.lang.reflect.Method;
import java.util.List;
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
	void spec1RequiresGivenBeforeItCanBeBuilt() {
		assertThatThrownBy(() -> Spec1.check().orFailWith("failure").done())
				.isInstanceOf(RuntimeException.class);
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
		assertBuilderHasSingularMethods(
				Spec4.check(), "whenFieldMatches", "thenFieldShouldMatch");
	}

	private static void assertBuilderHasSingularMethods(Object builder, String... expectedMethodNames) {
		final var methodNames =
				Stream.of(builder.getClass().getMethods()).map(Method::getName).toList();

		assertThat(methodNames).contains(expectedMethodNames);
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
