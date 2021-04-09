package org.revcloud.vader.types.validators;

import io.vavr.CheckedFunction1;

/**
 * Data type for Simple Validation functions. These functions work with Simple types as I/O.
 * This acts like a Typealias for {@link CheckedFunction1}
 *
 *  @author gakshintala
 *  @since 228
 */
@FunctionalInterface
public interface SimpleValidator<ValidatableT, FailureT> extends CheckedFunction1<ValidatableT, FailureT> {
}
