package org.qtc.delphinus.types;

import io.vavr.control.Either;

import java.util.function.UnaryOperator;

/**
 * gakshintala created on 4/11/20.
 */
public interface Validator<FailureT, ValidatableT> extends UnaryOperator<Either<FailureT, ValidatableT>> {
}
