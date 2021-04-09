package org.revcloud.vader.types.validators;

import io.vavr.CheckedFunction1;
import io.vavr.control.Either;

/**
 * Data type for Validation functions.
 * This acts like a TypeAlias for {@link CheckedFunction1} 
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface Validator<ValidatableT, FailureT> extends CheckedFunction1<Either<FailureT, ValidatableT>, Either<FailureT, ?>> {
}
