/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

import static io.vavr.Function1.identity;

/**
 * These Strategies compose multiple validations and return a single function which can be applied on the Validatable.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
class Strategies {

    /**
     * Higher-order function to compose list of validators into Fail-Fast Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> {
            if (validatable == null) return Either.left(invalidValidatable);
            // This casting is safe
            return applyValidations(validatable, validations)
                    .filter(Either::isLeft)
                    .getOrElse(Either.right(validatable));
        };
    }

    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<Validator<ValidatableT, FailureT>> validations, 
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> {
            if (validatable == null) return Either.left(invalidValidatable);
            // This casting is safe
            return applyValidations(validatable, validations, throwableMapper)
                    .filter(Either::isLeft)
                    .getOrElse(Either.right(validatable));
        };
    }

    /**
     * Higher-order function to compose list of Simple validators into Fail-Fast Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validations, FailureT invalidValidatable, FailureT none) {
        return validatable -> validatable == null
                ? invalidValidatable
                : applySimpleValidations(validatable, validations).filter(result -> result != none).getOrElse(none);
    }

    /**
     * Higher-order function to compose list of validators into Accumulation Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Accumulation Strategy
     */
    static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? List.of(Either.left(invalidValidatable))
                : applyValidations(validatable, validations)
                .map(result -> result.map(ignore -> validatable)) // replacing wildcard on right state with validatable.
                .toList();
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ValidatableT>> applyValidations(
            ValidatableT toBeValidated,
            List<Validator<ValidatableT, FailureT>> validations) {
        Either<FailureT, ValidatableT> toBeValidatedRight = Either.right(toBeValidated);
        // This is just returning the description, nothing shall be run without terminal operator.
        return validations.iterator()
                .map(currentValidation -> currentValidation.apply(toBeValidatedRight))
                .map(ignore -> toBeValidatedRight);
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ValidatableT>> applyValidations(
            ValidatableT toBeValidated, 
            List<Validator<ValidatableT, FailureT>> validations,
            Function1<Throwable, FailureT> throwableMapper) {
        Either<FailureT, ValidatableT> toBeValidatedRight = Either.right(toBeValidated);
        // This is just returning the description, nothing shall be run without terminal operator.
        return validations.iterator()
                .map(currentValidation -> fireValidation(currentValidation, toBeValidatedRight, throwableMapper))
                .map(ignore -> toBeValidatedRight);
    }

    private static <FailureT, ValidatableT> Either<FailureT, ?> fireValidation(
            Validator<ValidatableT, FailureT> validation,
            Either<FailureT, ValidatableT> validatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return Try.of(() -> validation.apply(validatable))
                .fold(throwable -> Either.left(throwableMapper.apply(throwable)), identity());
    }

    private static <FailureT, ValidatableT> Iterator<FailureT> applySimpleValidations(
            ValidatableT toBeValidated, List<SimpleValidator<ValidatableT, FailureT>> validations) {
        return validations.iterator()
                .map(validation -> validation.apply(toBeValidated));
    }

}

@FunctionalInterface
interface AccumulationStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, List<Either<FailureT, ValidatableT>>> {
}

@FunctionalInterface
interface FailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
}

@FunctionalInterface
interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
