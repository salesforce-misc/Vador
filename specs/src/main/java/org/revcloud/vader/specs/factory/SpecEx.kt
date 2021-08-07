@file:JvmName("SpecEx")

package org.revcloud.vader

import org.revcloud.vader.specs.specs.Spec1
import org.revcloud.vader.specs.specs.Spec2
import org.revcloud.vader.specs.specs.Spec3
import java.util.function.Predicate

internal fun <ValidatableT, FailureT, GivenT> Spec1<ValidatableT, FailureT, GivenT>.toPredicateEx(): Predicate<ValidatableT?> =
  Predicate { validatable ->
    val givenValue: GivenT = given.apply(validatable)
    shouldMatchAnyOf.any { it.matches(givenValue) } ||
      shouldMatchAnyOfFields.any { it.apply(validatable) == givenValue }
  }

internal fun <ValidatableT, FailureT, WhenT, ThenT> Spec2<ValidatableT, FailureT, WhenT, ThenT>.toPredicateEx(): Predicate<ValidatableT?> {
  return Predicate { validatable ->
    if ((matchesAnyOf.isNotEmpty() || shouldMatchAnyOf.isNotEmpty()) && (shouldRelateWith.isNotEmpty() || shouldRelateWithFn != null)) {
      throw IllegalArgumentException("`when-matches/matchesAnyOf + then-shouldMatch/shouldMatchAnyOf` cannot be given along with `shouldRelateWith` or `shouldRelateWithFn`")
    }
    val whenValue = `when`.apply(validatable)
    if (shouldRelateWith.isEmpty() && shouldRelateWithFn == null &&
      matchesAnyOf.none { it.matches(whenValue) }
    ) {
      return@Predicate true
    }
    val thenValue = then.apply(validatable)
    if (shouldRelateWith.isEmpty() && shouldRelateWithFn == null &&
      shouldMatchAnyOf.any { it.matches(thenValue) }
    ) {
      return@Predicate true
    }
    val validThenValues = shouldRelateWith[whenValue]
    // TODO 06/05/21 gopala.akshintala: This is a hack, as ImmutableCollections.$Set12.contains(null) throws NPE 
    if (validThenValues?.any { thenValue == it } == true) {
      return@Predicate true
    }
    shouldRelateWithFn?.apply(whenValue, thenValue) ?: false
  }
}

internal fun <ValidatableT, FailureT, WhenT, Then1T, Then2T> Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>.toPredicateEx(): Predicate<ValidatableT?> =
  Predicate { validatable ->
    val whenValue = `when`.apply(validatable)
    if (matchesAnyOf.none { it.matches(whenValue) }) {
      return@Predicate true
    }
    val thenValue1 = thenField1.apply(validatable)
    val thenValue2 = thenField2.apply(validatable)
    val validThen2Values = shouldRelateWith[thenValue1]
    // TODO 06/05/21 gopala.akshintala: This is a hack, as ImmutableCollections.$Set12.contains(null) throws NPE
    if (validThen2Values?.any { thenValue2 == it } == true) {
      return@Predicate true
    }
    if (shouldRelateWithFn?.apply(thenValue1, thenValue2) == true) {
      return@Predicate true
    }
    orField1ShouldMatchAnyOf.any { it.matches(thenValue1) } ||
      orField2ShouldMatchAnyOf.any { it.matches(thenValue2) }
  }
