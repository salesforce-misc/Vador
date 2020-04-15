package org.qtc.delphinus.types;
import io.vavr.control.Either;

import java.util.function.Function;

/**
 * gakshintala created on 4/14/20.
 */
public interface FailFastStrategy<FailureT, ValidatableT> extends Function<ValidatableT, Either<FailureT, ValidatableT>> {
}
