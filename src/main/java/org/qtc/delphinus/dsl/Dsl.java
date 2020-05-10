package org.qtc.delphinus.dsl;

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

/**
 * gakshintala created on 4/15/20.
 */
@UtilityClass
public class Dsl {

    /**
     * -------------------- SIMPLE --------------------
     **/

    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftSimple(
            SimpleValidator<ValidatableT, FailureT> toBeLifted, FailureT none) {
        return toBeValidated -> toBeValidated.flatMap(validatable -> {
            val result = toBeLifted.apply(validatable);
            return result != none ? Either.left(result) : Either.right(validatable);
        });
    }

    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllSimple(
            List<SimpleValidator<ValidatableT, FailureT>> toBeLiftedFns, FailureT none) {
        return toBeLiftedFns.map(toBeLifted -> liftSimple(toBeLifted, none));
    }

    /**
     * -------------------- SIMPLE THROWABLE --------------------
     **/

    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftSimpleThrowable(
            SimpleThrowableValidator<ValidatableT, FailureT> toBeLifted, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        ThrowableValidator<ValidatableT, FailureT> throwableValidator =
                validatable -> validatable.mapLeft(Either::<Throwable, FailureT>right)
                        .flatMap(toBeValidated -> {
                            val result = liftTry(toBeLifted).apply(toBeValidated).toEither();
                            return result.fold(
                                    throwable -> Either.left(Either.left(throwable)),
                                    failure -> failure != none ? Either.left(Either.right(failure)) : Either.right(toBeValidated));
                        });
        return liftThrowable(throwableValidator, throwableMapper);
    }

    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllSimpleThrowable(
            List<SimpleThrowableValidator<ValidatableT, FailureT>> toBeLiftedFns, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        return toBeLiftedFns.map(toBeLifted -> liftSimpleThrowable(toBeLifted, none, throwableMapper));
    }
    
    /**
     * -------------------- THROWABLE --------------------
     **/

    public static <FailureT, ValidatableT> Validator<ValidatableT, FailureT> liftThrowable(
            ThrowableValidator<ValidatableT, FailureT> throwableValidator, Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> {
            val result = throwableValidator.apply(validatable);
            return fold(result, throwableMapper);
        };
    }

    public static <FailureT, ValidatableT> List<Validator<ValidatableT, FailureT>> liftAllThrowable(
            List<ThrowableValidator<ValidatableT, FailureT>> toBeLiftedFns, Function1<Throwable, FailureT> throwableMapper) {
        return toBeLiftedFns.map(toBeLifted -> liftThrowable(toBeLifted, throwableMapper));
    }

    private static <FailureT, ValidatableT> Either<FailureT, ValidatableT> fold(
            Either<Either<Throwable, FailureT>, ValidatableT> throwableFailure, Function1<Throwable, FailureT> throwableMapper) {
        return throwableFailure.fold(
                throwableOrFailure ->
                        Either.left(throwableOrFailure.fold(
                                throwableMapper,
                                Function1.identity()
                        )),
                Either::right);
    }
}
