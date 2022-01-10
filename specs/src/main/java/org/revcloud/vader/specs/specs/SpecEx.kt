@file:JvmName("SpecEx")

package org.revcloud.vader.specs.specs

import org.revcloud.vader.specs.component1
import org.revcloud.vader.specs.component2
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
    if (shouldRelateWith.isEmpty() &&
      shouldRelateWithFn == null &&
      matchesAnyOf.none { it.matches(whenValue) }
    ) {
      return@Predicate true
    }
    val thenValue = then.apply(validatable)
    if (shouldRelateWith.isEmpty() &&
      shouldRelateWithFn == null &&
      shouldMatchAnyOf.any { it.matches(thenValue) }
    ) {
      return@Predicate true
    }
    val validThenValues = shouldRelateWith[whenValue]
    // TODO 06/05/21 gopala.akshintala: This is a hack, as `ImmutableCollections.$Set12.contains(thenValue)` throws NPE if `thenValue` is null.
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

internal fun <ValidatableT, FailureT, WhenT, Then1T, Then2T> Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>.getFailureEx(validatable: ValidatableT?): FailureT? {
  require((orFailWith == null) != (orFailWithFn == null)) {
    String.format(BaseSpec.INVALID_FAILURE_CONFIG, nameForTest)
  }
  return if (orFailWith != null) {
    orFailWith
  } else {
    orFailWithFn?.apply(
      `when`.apply(validatable),
      thenField1.apply(validatable),
      thenField2.apply(validatable)
    )
  }
}

internal fun <ValidatableT, FailureT> Spec4<ValidatableT, FailureT>.toPredicateEx(): Predicate<ValidatableT?> {
  return Predicate { validatable ->
    val whenAllFieldsMatch = whenFieldsMatch.all { (mapper, matcher) -> matcher?.matches(mapper?.apply(validatable)) ?: true }
    if (!whenAllFieldsMatch) {
      return@Predicate true
    }
    thenFieldsShouldMatch.all { (mapper, matcher) -> matcher?.matches(mapper?.apply(validatable)) ?: true }
  }
}

internal fun <ValidatableT, FailureT> Spec5<ValidatableT, FailureT>.toPredicateEx(): Predicate<ValidatableT?> {
  return Predicate { validatable ->
    val (whenFieldMappers, whenMatcher) = whenAllFieldsMatch
    val doesAllFieldsMatch = whenFieldMappers?.all { whenMapper -> whenMatcher?.matches(whenMapper?.apply(validatable)) ?: true } ?: false
    if (!doesAllFieldsMatch) {
      return@Predicate true
    }
    val (thenFieldMappers, thenMatcher) = thenAllFieldsShouldMatch
    thenFieldMappers?.all { thenMapper -> thenMatcher?.matches(thenMapper?.apply(validatable)) ?: true } ?: false
  }
}
