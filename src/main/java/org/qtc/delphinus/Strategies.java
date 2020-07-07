/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.types.strategies.AccumulationStrategy;
import org.qtc.delphinus.types.strategies.FailFastStrategy;
import org.qtc.delphinus.types.strategies.SimpleFailFastStrategy;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

/**
 * These Strategies compose multiple validations and return a single function which can be applied on the Validatable.
 *
 *  @author gakshintala
 *  @since 228
 */
@UtilityClass
public class Strategies {

    /**
     * Higher-order function to compose list of validators into Fail-Fast Strategy.
     * 
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return  Composed Fail-Fast Strategy
     */
    public static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? Either.left(invalidValidatable)
                : applyValidations(validatable, validations).getOrElse(Either.right(validatable));
    }

    /**
     * Higher-order function to compose list of Simple validators into Fail-Fast Strategy.
     * 
     * @param validations
     * @param invalidValidatable
     * @param none                  Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return  Composed Fail-Fast Strategy
     */
    public static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validations, FailureT invalidValidatable, FailureT none) {
        return validatable -> validatable == null
                ? invalidValidatable
                : applySimpleValidations(validatable, validations, none).getOrElse(none);
    }

    /**
     * Higher-order function to compose list of validators into Accumulation Strategy.
     * 
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return  Composed Accumulation Strategy
     */
    public static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? List.of(Either.left(invalidValidatable))
                : applyValidations(validatable, validations).toList();
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ?>> applyValidations(
            ValidatableT toBeValidated, List<Validator<ValidatableT, FailureT>> validations) {
        Either<FailureT, ValidatableT> toBeValidatedRight = Either.right(toBeValidated);
        final Iterator<Either<FailureT, ?>> validationResults =
                validations.iterator()
                        .map(validation -> validation.apply(toBeValidatedRight));
        // This is just returning the description, nothing shall be run without terminal operator.
        return validationResults.filter(Either::isLeft);
    }

    private static <FailureT, ValidatableT> Iterator<FailureT> applySimpleValidations(
            ValidatableT toBeValidated, List<SimpleValidator<ValidatableT, FailureT>> validations, FailureT none) {
        return validations.iterator()
                .map(validation -> validation.apply(toBeValidated)).filter(result -> result != none);
    }

}
