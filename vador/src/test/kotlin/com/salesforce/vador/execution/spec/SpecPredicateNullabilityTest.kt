/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.execution.spec

import com.salesforce.vador.specs.specs.Spec2
import com.salesforce.vador.specs.specs.Spec3
import com.salesforce.vador.specs.specs.Spec4
import com.salesforce.vador.specs.specs.Spec5
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import java.util.function.Predicate
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo

class SpecPredicateNullabilityTest :
  FunSpec({
    test("Spec2 through Spec5 predicate inputs remain nullable in Kotlin") {
      val spec2Predicate: Predicate<Bean?> =
        Spec2.check<Bean, String, String, String>()
          .`when`(Function1 { "when" })
          .then(Function1 { "then" })
          .orFailWith("failure")
          .done()
          .toPredicate()
      val spec3Predicate: Predicate<Bean?> =
        Spec3.check<Bean, String, String, String, String>()
          .`when`(Function1 { "when" })
          .thenField1(Function1 { "then1" })
          .thenField2(Function1 { "then2" })
          .orFailWith("failure")
          .done()
          .toPredicate()
      val spec4Predicate: Predicate<Bean?> = Spec4.check<Bean, String>().done().toPredicate()
      val emptyRule: Tuple2<Collection<Function1<Bean, *>>?, Matcher<*>?> =
        Tuple.of(emptyList(), equalTo("unused"))
      val spec5Predicate: Predicate<Bean?> =
        Spec5.check<Bean, String>()
          .whenAllTheseFieldsMatch(emptyRule)
          .thenAllThoseFieldsShouldMatch(emptyRule)
          .done()
          .toPredicate()

      listOf(spec2Predicate, spec3Predicate, spec4Predicate, spec5Predicate).forEach {
        it.test(null) shouldBe true
      }
    }
  })

private class Bean
