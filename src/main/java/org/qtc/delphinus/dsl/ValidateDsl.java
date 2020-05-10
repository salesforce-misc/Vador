package org.qtc.delphinus.dsl;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.Strategies;
import org.qtc.delphinus.types.validators.Validator;

/**
 * gakshintala created on 4/21/20.
 */
@UtilityClass
public class ValidateDsl {
    public static <FailureT, ValidatableT> FailureT validateAndFailFast(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators, 
            FailureT invalidValidatable, FailureT none) {
        final Either<FailureT, ValidatableT> validationResult 
                = Strategies.failFastStrategy(validators, invalidValidatable).apply(validatable);
        return validationResult.fold(Function1.identity(), ignore -> none);
    }

    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators, 
            FailureT invalidValidatable) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable))
                .toList();
    }
}
