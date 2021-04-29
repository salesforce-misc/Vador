package org.revcloud.vader.lift;

import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class InheritanceLiftUtil {

    public static <ParentT, ValidatableT extends ParentT, FailureT> Validator<ValidatableT, FailureT> liftToChildValidatorType(
            Validator<ParentT, FailureT> parentValidator) {
        return childValidatable -> parentValidator.apply(Either.narrow(childValidatable));
    }

    public static <ParentT, ValidatableT extends ParentT, FailureT> List<Validator<ValidatableT, FailureT>> liftAllToChildValidatorType(
            Collection<Validator<ParentT, FailureT>> parentValidators) {
        return parentValidators.stream()
                .map(InheritanceLiftUtil::<ParentT, ValidatableT, FailureT>liftToChildValidatorType)
                .collect(Collectors.toList());
    }
}
