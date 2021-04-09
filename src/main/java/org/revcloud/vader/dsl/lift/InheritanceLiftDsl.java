package org.revcloud.vader.dsl.lift;

import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.Validator;

@UtilityClass
public class InheritanceLiftDsl {

    public static <ParentT, ValidatableT extends ParentT, FailureT> Validator<ValidatableT, FailureT> liftToChild(
            Validator<ParentT, FailureT> parentValidator) {
        return childValidatable -> parentValidator.apply(Either.narrow(childValidatable));
    }

    public static <ParentT, ValidatableT extends ParentT, FailureT> List<Validator<ValidatableT, FailureT>> liftAllToChild(
            List<Validator<ParentT, FailureT>> parentValidators) {
        return parentValidators.map(parentValidator -> childValidatable -> parentValidator.apply(Either.narrow(childValidatable)));
    }
}
