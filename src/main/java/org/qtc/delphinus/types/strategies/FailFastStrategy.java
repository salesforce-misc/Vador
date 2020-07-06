package org.qtc.delphinus.types.strategies;

import io.vavr.Function1;
import io.vavr.control.Either;

/**
 * Composed function for validations, for Fail-Fast Strategy.
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface FailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ?>> {
}
