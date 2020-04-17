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
                : applyValidations(validations, validatable).getOrElse(Either.right(validatable));
    }

    public static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable) {
        return validatable -> validatable == null
                ? List.of(Either.left(invalidValidatable))
                : applyValidations(validations, validatable).toList();
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ValidatableT>> applyValidations(
            List<Validator<ValidatableT, FailureT>> validations, ValidatableT validatable) {
        Either<FailureT, ValidatableT> validatableRight = Either.right(validatable);
        return validations.iterator()
                .map(validation -> validation.apply(validatableRight))
                .filter(Either::isLeft);
    }

}
