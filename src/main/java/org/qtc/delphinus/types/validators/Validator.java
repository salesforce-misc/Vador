package org.qtc.delphinus.types.validators;

import io.vavr.Function1;
import io.vavr.control.Either;

/**
 * gakshintala created on 4/11/20.
 */
@FunctionalInterface
public interface Validator<ValidatableT, FailureT> extends Function1<Either<FailureT, ValidatableT>, Either<FailureT, ?>> {
}
