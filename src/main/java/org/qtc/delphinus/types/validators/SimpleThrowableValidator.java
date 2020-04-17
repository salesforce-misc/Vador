package org.qtc.delphinus.types.validators;

import io.vavr.CheckedFunction1;

@FunctionalInterface
public interface SimpleThrowableValidator<ValidatableT, FailureT> extends CheckedFunction1<ValidatableT, FailureT> {
    
}
