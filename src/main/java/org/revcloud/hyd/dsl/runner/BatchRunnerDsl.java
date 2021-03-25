package org.revcloud.hyd.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import org.revcloud.hyd.dsl.lift.LiftDsl;
import org.revcloud.hyd.dsl.runner.config.BatchValidationConfig;
import org.revcloud.hyd.dsl.runner.config.ValidationConfig;
import org.revcloud.hyd.types.validators.Validator;
import org.revcloud.hyd.types.validators.SimpleValidator;

/**
 * DSL for different ways to run validations against a List of validatables (Batch).
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class BatchRunnerDsl {
    /**
     * This can be used to validate a batch of beans together. It Validates a list of
     * validatables against a list of Validations, in fail-fast mode, per validatable.
     *
     * @param validatables       List of validatables.
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables,
            List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable, throwableMapper))
                .toList();
    }

    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables,
            List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable, throwableMapper, validationConfig))
                .toList();
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
        return validatables.iterator()
                .map(Strategies.accumulationStrategy(validators, invalidValidatable, throwableMapper))
                .toList();
    }

    // --- SIMPLE API --- //

    /**
     * Validates a list of validatables against a list of Simple Validations, in fail-fast mode, per validatable.
     *
     * @param validatables
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    // TODO: 25/03/21 rename this method
    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return validateAndFailFast(validatables, LiftDsl.liftAllSimple(validators, none), invalidValidatable, throwableMapper);
    }

    public static <FailureT, ValidatableT> List<FailureT> validateAndFailFastForSimpleValidators(
            List<ValidatableT> validatables,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable, none, throwableMapper))
                .toList();
    }

    public static <FailureT, ValidatableT> List<FailureT> validateAndFailFastForSimpleValidators(
            List<ValidatableT> validatables,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable, none, throwableMapper, validationConfig))
                .toList();
    }

    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validateAndFailFast(validatables, LiftDsl.liftAllSimple(validators, none), invalidValidatable, throwableMapper, validationConfig);
    }

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
            List<ValidatableT> validatables, List<SimpleValidator<ValidatableT, FailureT>> simpleValidators,
            FailureT invalidValidatable, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        return validateAndAccumulateErrors(validatables, LiftDsl.liftAllSimple(simpleValidators, none), invalidValidatable, throwableMapper);
    }
}
