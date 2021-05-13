@file:JvmName("Extensions")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import io.vavr.Tuple2
import org.hamcrest.Matcher
import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.lift.liftSimple
import org.revcloud.vader.runner.SpecFactory.Spec1
import org.revcloud.vader.types.validators.Validator
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

internal operator fun <T1> Tuple2<T1, *>?.component1(): T1? = this?._1
internal operator fun <T2> Tuple2<*, T2>?.component2(): T2? = this?._2

internal fun <ValidatableT, FailureT, GivenT> Spec1<ValidatableT, FailureT, GivenT>.toPredicateEx(): Predicate<ValidatableT> =
    Predicate<ValidatableT> { validatable: ValidatableT ->
        val givenValue: GivenT = given.apply(validatable)
        shouldMatchAnyOf.stream().anyMatch { m: Matcher<out GivenT> -> m.matches(givenValue) } ||
                shouldMatchAnyOfFields.stream()
                    .anyMatch { expectedFieldMapper ->
                        expectedFieldMapper.apply(validatable) == givenValue
                    }
    }

internal fun <ValidatableT, FailureT, WhenT, ThenT> SpecFactory.Spec2<ValidatableT, FailureT, WhenT, ThenT>.toPredicateEx(): Predicate<ValidatableT> {
    return Predicate { validatable: ValidatableT ->
        val whenValue = `when`.apply(validatable)
        if (shouldRelateWith.isEmpty() && shouldRelateWithFn == null &&
            matchesAnyOf.stream()
                .noneMatch { m: Matcher<out WhenT>? -> m!!.matches(whenValue) }
        ) {
            return@Predicate true
        }
        val thenValue = then.apply(validatable)
        val isThenMatches = shouldMatchAnyOf.stream().anyMatch { m: Matcher<out ThenT>? ->
            m!!.matches(thenValue)
        }
        if (!isThenMatches) {
            val validThenValues = shouldRelateWith[whenValue]
            if (validThenValues != null) {
                // TODO 06/05/21 gopala.akshintala: This is a hack, as ImmutableCollections.$Set12.contains(null) throws NPE 
                return@Predicate validThenValues.stream()
                    .anyMatch { validThenValue: ThenT -> thenValue === validThenValue }
            }
            if (shouldRelateWithFn != null) {
                return@Predicate shouldRelateWithFn.apply(whenValue, thenValue)
            }
        }
        isThenMatches
    }
}

internal fun <ValidatableT, FailureT, WhenT, Then1T, Then2T> SpecFactory.Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>.toPredicateEx(): Predicate<ValidatableT> {
    return Predicate { validatable: ValidatableT ->
        val whenValue = `when`.apply(validatable)
        if (matchesAnyOf.stream().noneMatch { m: Matcher<out WhenT>? -> m!!.matches(whenValue) }) {
            return@Predicate true
        }
        val thenValue1 = thenField1.apply(validatable)
        val thenValue2 = thenField2.apply(validatable)
        var relationResult = false
        relationResult = if (shouldRelateWithFn != null) {
            shouldRelateWithFn.apply(thenValue1, thenValue2)
        } else {
            val validThen2Values = shouldRelateWith[thenValue1]
            validThen2Values != null && validThen2Values.contains(thenValue2)
        }
        relationResult ||
                orField1ShouldMatchAnyOf.stream().anyMatch { matcher: Matcher<out Then1T>? ->
                    matcher!!.matches(
                        thenValue1
                    )
                } ||
                orField2ShouldMatchAnyOf.stream().anyMatch { matcher: Matcher<out Then2T>? ->
                    matcher!!.matches(thenValue2) }
    }
}

fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT, FailureT>.getHeaderValidatorsStream(): Stream<Validator<HeaderValidatableT?, FailureT?>> {
    val withSimpleValidatorsOrFailWithStream = Stream.ofNullable(withSimpleHeaderValidatorsOrFailWith).flatMap { it!!.apply(::liftAllSimple).stream() }
    val withSimpleValidatorStream = withSimpleHeaderValidators.stream().map { it!!.apply(::liftSimple) }
    return Stream.of(withSimpleValidatorsOrFailWithStream, withSimpleValidatorStream, withHeaderValidators.stream()).flatMap { it }
}

fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT, FailureT>.getFieldNamesForBatch(validatableClazz: Class<HeaderValidatableT>): Set<String> {
    return withBatchMappers.stream().map { PropertyUtils.getPropertyName(validatableClazz, it) }.collect(Collectors.toSet())
}

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getValidatorsStream(): Stream<Validator<ValidatableT?, FailureT?>> {
    val simpleValidators = withSimpleValidatorsOrFailWith._1 + withSimpleValidators.map { it._1 }
    return Stream.concat(
        withValidators.stream(),
        liftAllSimple(simpleValidators, withSimpleValidatorsOrFailWith._2).stream()
    )
}

