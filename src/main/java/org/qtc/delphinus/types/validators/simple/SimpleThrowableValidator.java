package org.qtc.delphinus.types.validators.simple;

import io.vavr.CheckedFunction1;

/**
 *  Data type for Simple Throwable Validation functions. These functions work with Simple types as I/O.
 *
 *  @author gakshintala
 *  @since 228    
 */
@FunctionalInterface
public interface SimpleThrowableValidator<ValidatableT, FailureT> extends CheckedFunction1<ValidatableT, FailureT> {
}
