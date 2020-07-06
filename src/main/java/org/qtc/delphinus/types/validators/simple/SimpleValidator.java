package org.qtc.delphinus.types.validators.simple;

import io.vavr.Function1;

/**
 * Data type for Simple Validation functions. These functions work with Simple types as I/O.
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface SimpleValidator<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
