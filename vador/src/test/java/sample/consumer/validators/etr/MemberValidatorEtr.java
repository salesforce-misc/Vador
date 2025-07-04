/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.etr;

import com.salesforce.vador.types.ValidatorEtr;
import java.util.Objects;
import sample.consumer.bean.Member;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

public class MemberValidatorEtr {

	public static final ValidatorEtr<Member, ValidationFailure> validatorEtr1 =
			member ->
					member.filterOrElse(
							Objects::nonNull,
							ignore -> new ValidationFailure(ValidationFailureMessage.FIELD_NULL_OR_EMPTY));

	public static final ValidatorEtr<Member, ValidationFailure> validatorEtr2 =
			member ->
					member.filterOrElse(
							Objects::nonNull,
							ignore -> new ValidationFailure(ValidationFailureMessage.FIELD_NULL_OR_EMPTY));
}
