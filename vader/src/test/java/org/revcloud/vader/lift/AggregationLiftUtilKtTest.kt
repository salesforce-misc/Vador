package org.revcloud.vader.lift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.ValidatorEtr
import sample.consumer.failure.ValidationFailure
import sample.consumer.failure.ValidationFailure.NONE

class AggregationLiftUtilKtTest : FunSpec({

  test("liftToContainerValidatorType") {
    val memberValidator: ValidatorEtr<Member?, ValidationFailure?> = ValidatorEtr { right(NONE) }
    val liftedContainerValidator: ValidatorEtr<Container?, ValidationFailure?> =
      liftToContainerValidatorType(memberValidator) { it?.member }
    liftedContainerValidator.apply(right(Container(Member(0)))) shouldBe right(NONE)
  }
})

data class Member(var id: Int = 0)
data class Container(val member: Member = Member(0))
