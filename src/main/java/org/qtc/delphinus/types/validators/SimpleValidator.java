package org.qtc.delphinus.types.validators;

import io.vavr.Function1;

@FunctionalInterface
public interface SimpleValidator<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
