package org.qtc.delphinus.types.strategies;

import io.vavr.Function1;
import io.vavr.control.Either;

/**
 * gakshintala created on 4/14/20.
 */
@FunctionalInterface
public interface FailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ?>> {
}
