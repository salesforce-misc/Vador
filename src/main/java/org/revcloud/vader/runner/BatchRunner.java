package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.lift.ValidatorLiftUtil;
import org.revcloud.vader.lift.ValidatorLiftUtil.*;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DSL for different ways to run validations against a List of validatables (Batch).
 * <p>
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class BatchRunner {
    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFastForBatch(
            List<ValidatableT> validatables,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        return FailFastStrategies.failFastForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
                .apply(validatables);
    }

    public static <FailureT, ValidatableT, PairT> List<Either<Tuple2<PairT, FailureT>, ValidatableT>> validateAndFailFastForBatch(
            List<ValidatableT> validatables,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig,
            Function1<ValidatableT, PairT> pairForInvalidMapper) {
        val validationResults = FailFastStrategies.failFastForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
                .apply(validatables);
        return io.vavr.collection.List.ofAll(validationResults).zipWith(validatables, 
                (result, validatable) -> result.mapLeft(failure -> Tuple.of(pairForInvalidMapper.apply(validatable), failure))).toJavaList();
    }

    public static <FailureT, ValidatableT> Optional<FailureT> validateAndFailFastAllOrNoneForBatch(
            List<ValidatableT> validatables,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        return FailFastStrategies.failFastAllOrNoneForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
                .apply(validatables);
    }

    // --- ERROR ACCUMULATION ---
    /**
     * Validates a list of validatables against a list of Simple validations, in error-accumulation mode, per validatable.
     *
     * @param validatables
     * @param simpleValidators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<List<Either<FailureT, ValidatableT>>> validateAndAccumulateErrors(
            List<ValidatableT> validatables,
            List<SimpleValidator<ValidatableT, FailureT>> simpleValidators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return validateAndAccumulateErrors(validatables, ValidatorLiftUtil.liftAllSimple(simpleValidators, none), invalidValidatable, throwableMapper);
    }

    /**
     * Validates a list of validatables against a list of validations, in error-accumulation mode, per validatable.
     *
     * @param validatables
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<List<Either<FailureT, ValidatableT>>> validateAndAccumulateErrors(
            List<ValidatableT> validatables,
            List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatables.stream()
                .map(AccumulationStrategies.accumulationStrategy(validators, invalidValidatable, throwableMapper))
                .collect(Collectors.toList());
    }
}
