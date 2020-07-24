package org.qtc.delphinus.dsl.lift;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.qtc.delphinus.types.validators.ThrowableValidator;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleThrowableValidator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

import static io.vavr.CheckedFunction1.liftTry;
import static io.vavr.Function1.identity;

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
        return toBeValidated -> toBeValidated.flatMap(validatable -> {
            val result = toBeLifted.apply(validatable);
            return result != none ? Either.left(result) : Either.right(validatable);
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

    /**
     * Lifts a Simple Throwable validator to Validator type.
     * 
     * @param toBeLifted        Throwable Validator to be lifted
     * @param none              Value to be returned in case of no failure.
     * @param throwableMapper   Mapper function to convert throwable to FailureT
     * @param <FailureT>
     * @param <ValidatableT>
     * @return                  Validator            
     */
    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftSimpleThrowable(
            SimpleThrowableValidator<ValidatableT, FailureT> toBeLifted, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> validatable
                .flatMap(toBeValidated -> {
                    val result = liftTry(toBeLifted).apply(toBeValidated).toEither();
                    return result.fold(
                            throwable -> Either.left(throwableMapper.apply(throwable)),
                            failure -> failure != none ? Either.left(failure) : Either.right(toBeValidated));
                });
    }

    /**
     * Lifts a list of Simple Throwable Validations to list of Validators.
     * 
     * @param toBeLiftedFns     List of Simple Throwable Validators to be lifted.
     * @param none              Value to be returned in case of no failure.
     * @param throwableMapper   Mapper function to convert throwable to FailureT
     * @param <FailureT>
     * @param <ValidatableT>
     * @return                  List of Validators
     */
    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllSimpleThrowable(
            List<SimpleThrowableValidator<ValidatableT, FailureT>> toBeLiftedFns, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        return toBeLiftedFns.map(toBeLifted -> liftSimpleThrowable(toBeLifted, none, throwableMapper));
    }

    /**
     * Lifts a Throwable Validator to Validator type.
     * 
     * @param toBeLifted        Throwable Validator to be lifted.
     * @param throwableMapper   Mapper function to convert throwable to FailureT
     * @param <FailureT>
     * @param <ValidatableT>
     * @return                  Validator
     */
    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftThrowable(
            ThrowableValidator<ValidatableT, FailureT> toBeLifted, Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> {
            val result = liftTry(toBeLifted).apply(validatable).toEither();
            return result.fold(throwable -> Either.left(throwableMapper.apply(throwable)), identity());
        };
    }

    /**
     * Lifts a list of Throwable Validations to list of Validators.
     * 
     * @param toBeLiftedFns     List of Simple Throwable Validators to be lifted
     * @param throwableMapper   Mapper function to convert throwable to FailureT
     * @param <FailureT>
     * @param <ValidatableT>
     * @return                  List of Validators
     */
    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllThrowable(
            List<ThrowableValidator<ValidatableT, FailureT>> toBeLiftedFns, Function1<Throwable, FailureT> throwableMapper) {
        return toBeLiftedFns.map(toBeLifted -> liftThrowable(toBeLifted, throwableMapper));
    }
}
