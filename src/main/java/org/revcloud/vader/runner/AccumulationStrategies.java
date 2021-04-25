package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.Validator;

/**
 * These Accumulation compose multiple validations and return a single function which can be applied on the Validatable.
 * <p>
 * TODO: Cleanup this class, this is too clumsy
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
class AccumulationStrategies {

    /**
     * Higher-order function to compose list of validators into Accumulation Strategy.
     *
     * @param validators
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Accumulation Strategy
     */
    static <FailureT, ValidatableT> Accumulation<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validators, FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> toBeValidated == null
                ? List.of(Either.left(invalidValidatable))
                : List.ofAll(Utils.fireValidators(Either.right(toBeValidated), validators.toJavaStream(), throwableMapper));
    }

    @FunctionalInterface
    interface Accumulation<ValidatableT, FailureT> extends Function1<ValidatableT, List<Either<FailureT, ValidatableT>>> {
    }
}
