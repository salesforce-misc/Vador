package org.qtc.delphinus.types.validators;

import io.vavr.CheckedFunction1;
import io.vavr.control.Either;

/**
 * gakshintala created on 4/11/20.
 */
@FunctionalInterface
public interface ThrowableValidator<ValidatableT, FailureT> 
        extends CheckedFunction1<Either<FailureT, ValidatableT>, Either<FailureT, ?>> {
}
