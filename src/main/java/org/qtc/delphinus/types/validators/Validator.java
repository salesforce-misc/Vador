package org.qtc.delphinus.types.validators;

import io.vavr.Function1;
import io.vavr.control.Either;

/**
 * Data type for Validation functions.
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface Validator<ValidatableT, FailureT> extends Function1<Either<FailureT, ValidatableT>, Either<FailureT, ?>> {
}
