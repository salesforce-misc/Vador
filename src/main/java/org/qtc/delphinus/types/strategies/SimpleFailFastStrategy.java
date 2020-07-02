package org.qtc.delphinus.types.strategies;

import io.vavr.Function1;

/**
 * Composed function for all simple validations.
 * 
 * gakshintala created on 5/1/20.
 */
@FunctionalInterface
public interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
