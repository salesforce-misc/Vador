/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vador.lift;

import static org.revcloud.vador.lift.AggregationLiftEtrUtil.liftToContainerValidatorType;

import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vador.types.ValidatorEtr;
import sample.consumer.bean.Container;
import sample.consumer.bean.Member;
import sample.consumer.failure.ValidationFailure;

class AggregationLiftEtrUtilTest {

  @Test
  void liftToContainerValidationType() {
    final var failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
    var memberValidator =
        (ValidatorEtr<? super Member, ? extends ValidationFailure>) member -> failure;
    final var liftedContainerValidator =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    final var toBeValidated = new Container(0, new Member(0));
    Assertions.assertSame(
        failure, liftedContainerValidator.unchecked().apply(Either.right(toBeValidated)));
  }
}
