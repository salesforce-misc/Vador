/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.lift

import com.salesforce.vador.types.ValidatorEtr
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.kotlin.right
import sample.consumer.failure.ValidationFailure
import sample.consumer.failure.ValidationFailure.NONE

class AggregationLiftUtilKtTest :
  FunSpec({
    test("liftToContainerValidatorType") {
      val memberValidator: ValidatorEtr<Member?, ValidationFailure?> = ValidatorEtr { right(NONE) }
      val liftedContainerValidator: ValidatorEtr<Container?, ValidationFailure?> =
        liftToContainerValidatorType(memberValidator) { it?.member }
      liftedContainerValidator.apply(right(Container(Member(0)))) shouldBe right(NONE)
    }
  })

data class Member(var id: Int = 0)

data class Container(val member: Member = Member(0))
