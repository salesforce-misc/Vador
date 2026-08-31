/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.specs.specs

import io.vavr.Function1
import org.hamcrest.Matcher

interface Spec1BuilderDsl<ValidatableT, GivenT, SELF> {
  fun shouldMatchAnyOfFields(element: Function1<ValidatableT, *>): SELF

  fun shouldMatchField(element: Function1<ValidatableT, *>): SELF = shouldMatchAnyOfFields(element)

  fun shouldMatchAnyOf(element: Matcher<out GivenT>): SELF

  fun shouldMatch(element: Matcher<out GivenT>): SELF = shouldMatchAnyOf(element)
}

interface Spec2BuilderDsl<WhenT, ThenT, SELF> {
  fun matchesAnyOf(element: Matcher<out WhenT>): SELF

  fun matches(element: Matcher<out WhenT>): SELF = matchesAnyOf(element)

  fun shouldMatchAnyOf(element: Matcher<out ThenT>): SELF

  fun shouldMatch(element: Matcher<out ThenT>): SELF = shouldMatchAnyOf(element)

  fun shouldRelateWith(key: WhenT, value: Set<@JvmSuppressWildcards ThenT>): SELF

  fun shouldRelateWithEntry(key: WhenT, value: Set<ThenT>): SELF = shouldRelateWith(key, value)

  fun putAllShouldRelateWith(entries: Map<out WhenT, Set<@JvmSuppressWildcards ThenT>>): SELF

  fun shouldRelateWith(vararg entries: Map<out WhenT, Set<ThenT>>): SELF {
    val merged = linkedMapOf<WhenT, Set<ThenT>>()
    entries.forEach(merged::putAll)
    return putAllShouldRelateWith(merged)
  }
}

interface Spec3BuilderDsl<WhenT, Then1T, Then2T, SELF> {
  fun matchesAnyOf(element: Matcher<out WhenT>): SELF

  fun matches(element: Matcher<out WhenT>): SELF = matchesAnyOf(element)

  fun shouldRelateWith(key: Then1T, value: Set<@JvmSuppressWildcards Then2T>): SELF

  fun shouldRelateWithEntry(key: Then1T, value: Set<Then2T>): SELF = shouldRelateWith(key, value)

  fun putAllShouldRelateWith(entries: Map<out Then1T, Set<@JvmSuppressWildcards Then2T>>): SELF

  fun shouldRelateWith(vararg entries: Map<out Then1T, Set<Then2T>>): SELF {
    val merged = linkedMapOf<Then1T, Set<Then2T>>()
    entries.forEach(merged::putAll)
    return putAllShouldRelateWith(merged)
  }

  fun orField1ShouldMatchAnyOf(element: Matcher<out Then1T>): SELF

  fun orField1ShouldMatch(element: Matcher<out Then1T>): SELF = orField1ShouldMatchAnyOf(element)

  fun orField2ShouldMatchAnyOf(element: Matcher<out Then2T>): SELF

  fun orField2ShouldMatch(element: Matcher<out Then2T>): SELF = orField2ShouldMatchAnyOf(element)
}

interface Spec4BuilderDsl<ValidatableT, SELF> {
  fun whenTheseFieldsMatch(key: Function1<ValidatableT, *>, value: Matcher<*>): SELF

  fun whenFieldMatches(key: Function1<ValidatableT, *>, value: Matcher<*>): SELF =
    whenTheseFieldsMatch(key, value)

  fun thenThoseFieldsShouldMatch(key: Function1<ValidatableT, *>, value: Matcher<*>): SELF

  fun thenFieldShouldMatch(key: Function1<ValidatableT, *>, value: Matcher<*>): SELF =
    thenThoseFieldsShouldMatch(key, value)
}
