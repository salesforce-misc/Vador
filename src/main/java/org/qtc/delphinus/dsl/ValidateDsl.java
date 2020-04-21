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
    public static <FailureT, ValidatableT> FailureT nonBulkValidateFailFast(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validations, 
            FailureT invalidValidatable, FailureT none) {
        final Either<FailureT, ValidatableT> validationResult 
                = Strategies.failFastStrategy(validations, invalidValidatable).apply(validatable);
        return validationResult.fold(Function1.identity(), ignore -> none);
    }
}
