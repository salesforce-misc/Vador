@file:JvmName("Extensions")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import io.vavr.Tuple2
import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.lift.liftSimple
import org.revcloud.vader.runner.SpecFactory.Spec1
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator
import java.util.function.Predicate

internal operator fun <T1> Tuple2<T1, *>?.component1(): T1? = this?._1
internal operator fun <T2> Tuple2<*, T2>?.component2(): T2? = this?._2

internal fun <ValidatableT, FailureT, GivenT> Spec1<ValidatableT, FailureT, GivenT>.toPredicateEx(): Predicate<ValidatableT> =
    Predicate<ValidatableT> { validatable ->
        val givenValue: GivenT = given.apply(validatable)
        shouldMatchAnyOf.any { it.matches(givenValue) } ||
                shouldMatchAnyOfFields.any { it.apply(validatable) == givenValue }
    }

internal fun <ValidatableT, FailureT, WhenT, ThenT> SpecFactory.Spec2<ValidatableT, FailureT, WhenT, ThenT>.toPredicateEx(): Predicate<ValidatableT> {
    return Predicate { validatable: ValidatableT ->
        val whenValue = `when`.apply(validatable)
        if (shouldRelateWith.isEmpty() && shouldRelateWithFn == null && matchesAnyOf.none { it.matches(whenValue) }) {
            return@Predicate true
        }
        val thenValue = then.apply(validatable)
        if (shouldMatchAnyOf.any { it.matches(thenValue) }) {
            return@Predicate true
        }
        val validThenValues = shouldRelateWith[whenValue]
        if (validThenValues != null && validThenValues.any { thenValue == it }) {
            // TODO 06/05/21 gopala.akshintala: This is a hack, as ImmutableCollections.$Set12.contains(null) throws NPE 
            return@Predicate true
        }
        shouldRelateWithFn?.apply(whenValue, thenValue) ?: false
    }
}

internal fun <ValidatableT, FailureT, WhenT, Then1T, Then2T> SpecFactory.Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>.toPredicateEx(): Predicate<ValidatableT> =
    Predicate { validatable ->
        val whenValue = `when`.apply(validatable)
        if (matchesAnyOf.none { it.matches(whenValue) }) {
            return@Predicate true
        }
        val thenValue1 = thenField1.apply(validatable)
        val thenValue2 = thenField2.apply(validatable)
        if (shouldRelateWithFn != null && shouldRelateWithFn.apply(thenValue1, thenValue2)) {
            return@Predicate true
        }
        val validThen2Values = shouldRelateWith[thenValue1]
        if (validThen2Values != null && validThen2Values.any { thenValue2 == it }) {
            // TODO 06/05/21 gopala.akshintala: This is a hack, as ImmutableCollections.$Set12.contains(null) throws NPE
            return@Predicate true
        }
        orField1ShouldMatchAnyOf.any { it.matches(thenValue1) } ||
                orField2ShouldMatchAnyOf.any { it.matches(thenValue2) }
    }

fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT?, FailureT?>.getHeaderValidatorsEx(): List<Validator<HeaderValidatableT?, FailureT?>> =
    fromSimpleValidators1(withSimpleHeaderValidators) + fromSimpleValidators2(withSimpleHeaderValidator) + withHeaderValidators

fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT?, FailureT?>.getFieldNamesForBatchEx(
    validatableClazz: Class<HeaderValidatableT>
): Set<String> = withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getValidators(): List<Validator<ValidatableT?, FailureT?>> =
    fromSimpleValidators1(withSimpleValidators) + fromSimpleValidators2(withSimpleValidator) + withValidators

private fun <ValidatableT, FailureT> fromSimpleValidators1(simpleValidators: Tuple2<out Collection<SimpleValidator<ValidatableT?, FailureT?>>?, out FailureT?>?): List<Validator<ValidatableT?, FailureT?>> =
    simpleValidators?.let { (svs, none) -> svs?.let { liftAllSimple(it, none) } } ?: emptyList()

private fun <ValidatableT, FailureT> fromSimpleValidators2(simpleValidators: Collection<Tuple2<out SimpleValidator<ValidatableT?, FailureT?>, out FailureT?>>): List<Validator<ValidatableT?, FailureT?>> =
    simpleValidators.mapNotNull { (sv, none) -> sv?.let { liftSimple(it, none) } }
