package org.qtc.delphinus.types.validators;

import io.vavr.CheckedFunction1;
import io.vavr.control.Either;

/**
 * Data type for Throwable Validation functions.
 * 
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface ThrowableValidator<ValidatableT, FailureT> 
        extends CheckedFunction1<Either<FailureT, ValidatableT>, Either<FailureT, ?>> {
}
