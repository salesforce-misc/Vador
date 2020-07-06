package org.qtc.delphinus.types.strategies;

import io.vavr.Function1;

/**
 * Composed function for simple validations, for Fail-Fast Strategy.
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
