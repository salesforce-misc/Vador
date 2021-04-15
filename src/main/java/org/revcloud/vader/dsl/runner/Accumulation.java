package org.revcloud.vader.dsl.runner;

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
class Accumulation {

    /**
     * Higher-order function to compose list of validators into Accumulation Strategy.
     *
     * @param validators
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Accumulation Strategy
     */
    static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validators, FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> toBeValidated == null
                ? List.of(Either.left(invalidValidatable))
                : Utils.fireValidators(Either.right(toBeValidated), validators.iterator(), throwableMapper).toList();
    }

    @FunctionalInterface
    interface AccumulationStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, List<Either<FailureT, ValidatableT>>> {
    }
}