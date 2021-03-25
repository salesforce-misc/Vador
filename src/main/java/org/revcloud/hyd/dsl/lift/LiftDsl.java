package org.revcloud.hyd.dsl.lift;

import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.hyd.types.validators.Validator;
import org.revcloud.hyd.types.validators.SimpleValidator;

/**
 * Dsl to lift simple, throwable validations to Validator type.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class LiftDsl {

    /**
     * Lifts Simple Validator to Validator type.
     *
     * @param toBeLifted     Simple validator to be lifted
     * @param none           Value to be returned in case of no failure
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Validator
     */
    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftSimple(
            SimpleValidator<ValidatableT, FailureT> toBeLifted, FailureT none) {
        return validatable -> validatable.flatMap(toBeValidated -> {
            val result = toBeLifted.apply(toBeValidated);
            return result != none ? Either.left(result) : validatable;
        });
    }

    /**
     * Lifts a list of Simple validators to list of Validator type.
     * 
     * @param toBeLiftedFns List of Simple functions to be lifted.
     * @param none          Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return              List of Validators
     */
    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllSimple(
            List<SimpleValidator<ValidatableT, FailureT>> toBeLiftedFns, FailureT none) {
        return toBeLiftedFns.map(toBeLifted -> liftSimple(toBeLifted, none));
    }

}
