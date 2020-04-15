package org.qtc.delphinus.types;

import io.vavr.collection.List;
import io.vavr.control.Either;

import java.util.function.Function;

/**
 * gakshintala created on 4/14/20.
 */
public interface AccumulationStrategy<FailureT, ValidatableT> extends Function<ValidatableT, List<Either<FailureT, ValidatableT>>> {
}
