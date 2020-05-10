package org.qtc.delphinus.types.validators.simple;

import io.vavr.CheckedFunction1;

@FunctionalInterface
public interface SimpleThrowableValidator<ValidatableT, FailureT> extends CheckedFunction1<ValidatableT, FailureT> {
}
