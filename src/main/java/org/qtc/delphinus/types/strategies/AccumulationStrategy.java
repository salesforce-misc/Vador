package org.qtc.delphinus.types.strategies;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;

/**
 * Composed function for validations, for Error-Accumulation Strategy.
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface AccumulationStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, List<Either<FailureT, ?>>> {
}
