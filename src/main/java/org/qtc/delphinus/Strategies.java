/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.types.strategies.AccumulationStrategy;
import org.qtc.delphinus.types.strategies.FailFastStrategy;
import org.qtc.delphinus.types.validators.Validator;

/**
 * gakshintala created on 4/15/20.
 */
@UtilityClass
public class Strategies {

    public static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? Either.left(invalidValidatable)
                : applyValidations(validatable, validations).getOrElse(Either.right(validatable));
    }

    public static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? List.of(Either.left(invalidValidatable))
                : applyValidations(validatable, validations).toList();
    }

    private static <FailureT, ValidatableT> List<Either<FailureT, ?>> applyValidations(
            ValidatableT toBeValidated, List<Validator<ValidatableT, FailureT>> validations) {
        Either<FailureT, ValidatableT> toBeValidatedRight = Either.right(toBeValidated);
        final List<Either<FailureT, ?>> validationResults =
                validations
                        .map(validation -> validation.apply(toBeValidatedRight));
        return validationResults.filter(Either::isLeft);
    }

}
