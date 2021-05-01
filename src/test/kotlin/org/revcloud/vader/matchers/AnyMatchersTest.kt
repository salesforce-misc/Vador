package org.revcloud.vader.matchers

import io.kotest.core.datatest.forAll
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers

class AnyMatchersTest : FunSpec({

    test("anyOf") {
        anyOf(inRangeInclusive(1, 31), Matchers.nullValue()).matches(null) shouldBe true
        forAll(1 to 31) {
            anyOf(inRangeInclusive(1, 31), Matchers.nullValue()).matches(null) shouldBe true
        }
    }
})
