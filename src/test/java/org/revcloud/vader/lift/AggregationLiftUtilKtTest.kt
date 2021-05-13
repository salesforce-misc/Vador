package org.revcloud.vader.lift

import consumer.failure.ValidationFailure
import consumer.failure.ValidationFailure.NONE
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.Validator

class AggregationLiftUtilKtTest : FunSpec({

    test("liftToContainerValidatorType") {
        val memberValidator: Validator<Member?, ValidationFailure?> = Validator { right(NONE) }
        val liftedContainerValidator: Validator<Container?, ValidationFailure?> =
            liftToContainerValidatorType(memberValidator) { it?.member }
        liftedContainerValidator.apply(right(Container(Member(0)))) shouldBe right(NONE)
    }
})


data class Member(var id: Int = 0)
data class Container(val member: Member = Member(0))
