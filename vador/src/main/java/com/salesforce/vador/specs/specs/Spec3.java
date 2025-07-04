/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.specs.specs;

import com.salesforce.vador.specs.specs.base.BaseSpec;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;

@Value
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>
		extends BaseSpec<ValidatableT, FailureT> {

	@NonNull Function1<ValidatableT, ? extends WhenT> when;

	@Singular("matches")
	Collection<? extends Matcher<? extends WhenT>> matchesAnyOf;

	@NonNull Function1<ValidatableT, ? extends Then1T> thenField1;

	@NonNull Function1<ValidatableT, ? extends Then2T> thenField2;

	@Singular("shouldRelateWithEntry")
	Map<? extends Then1T, ? extends Set<? extends Then2T>> shouldRelateWith;

	@Nullable Function2<Then1T, Then2T, Boolean> shouldRelateWithFn;

	@Singular("orField1ShouldMatch")
	Collection<? extends Matcher<? extends Then1T>> orField1ShouldMatchAnyOf;

	@Singular("orField2ShouldMatch")
	Collection<? extends Matcher<? extends Then2T>> orField2ShouldMatchAnyOf;

	@Nullable Function3<WhenT, Then1T, Then2T, ? extends FailureT> orFailWithFn;

	@Override
	public Predicate<@Nullable ValidatableT> toPredicate() {
		return SpecEx.toPredicateEx(this);
	}

	@Override
	public FailureT getFailure(@Nullable ValidatableT validatable) {
		return SpecEx.getFailureEx(this, validatable);
	}
}
