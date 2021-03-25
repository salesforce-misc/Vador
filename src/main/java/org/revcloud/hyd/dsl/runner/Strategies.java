/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.hyd.dsl.runner;

import com.force.swag.id.ID;
import com.force.swag.id.IdTraits;
import io.vavr.Function1;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.hyd.dsl.runner.config.HeaderValidationConfig;
import org.revcloud.hyd.dsl.runner.config.ValidationConfig;
import org.revcloud.hyd.types.validators.SimpleValidator;
import org.revcloud.hyd.types.validators.Validator;

import java.util.function.Predicate;

import static io.vavr.Function1.identity;

/**
 * These Strategies compose multiple validations and return a single function which can be applied on the Validatable.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
class Strategies {

    /**
     * Higher-order function to compose list of validators into Fail-Fast Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            @NonNull List<Validator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            return applyValidations(toBeValidated, validations.iterator(), throwableMapper)
                    .filter(Either::isLeft)
                    .getOrElse(Either.right(toBeValidated)).map(ignore -> toBeValidated);
        };
    }

    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            @NonNull List<Validator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper, ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            toValidations(validationConfig);
            return applyValidations(toBeValidated, Iterator.concat(toValidations(validationConfig), validations), throwableMapper)
                    .filter(Either::isLeft)
                    .getOrElse(Either.right(toBeValidated)).map(ignore -> toBeValidated);
        };
    }

    private static <ValidatableT, FailureT> Iterator<Validator<ValidatableT, FailureT>> toValidations(
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        Iterator<Validator<ValidatableT, FailureT>> mandatoryFieldValidations = validationConfig.getMandatoryFieldMappers().iterator()
                .map(tuple2 -> validatableRight -> validatableRight.map(tuple2._1)
                        .filterOrElse(isPresent, ignore -> tuple2._2));
        Iterator<Validator<ValidatableT, FailureT>> sfIdValidations = validationConfig.getSfIdFieldMappers().iterator()
                .map(tuple2 -> validatableRight -> validatableRight.map(tuple2._1).map(ID::toString)
                        .filterOrElse(IdTraits::isValidId, ignore -> tuple2._2));

        return mandatoryFieldValidations.concat(sfIdValidations);
    }

    public static final Predicate<Object> isPresent = fieldValue -> {
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                return !((String) fieldValue).isBlank();
            }
            return true;
        }
        return false;
    };

    /**
     * Higher-order function to compose list of validators into Accumulation Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Accumulation Strategy
     */
    static <FailureT, ValidatableT> AccumulationStrategy<ValidatableT, FailureT> accumulationStrategy(
            List<Validator<ValidatableT, FailureT>> validations, FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> toBeValidated == null
                ? List.of(Either.left(invalidValidatable))
                : applyValidations(toBeValidated, validations.iterator(), throwableMapper)
                .map(result -> result.map(ignore -> toBeValidated))
                .toList();
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ?>> applyValidations(
            ValidatableT toBeValidated,
            Iterator<Validator<ValidatableT, FailureT>> validations,
            Function1<Throwable, FailureT> throwableMapper) {
        Either<FailureT, ValidatableT> toBeValidatedRight = Either.right(toBeValidated);
        // This is just returning the description, nothing shall be run without terminal operator.
        return validations
                .map(currentValidation -> fireValidation(currentValidation, toBeValidatedRight, throwableMapper));
    }

    private static <FailureT, ValidatableT> Either<FailureT, ?> fireValidation(
            Validator<ValidatableT, FailureT> validation,
            Either<FailureT, ValidatableT> validatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return Try.of(() -> validation.apply(validatable))
                .fold(throwable -> Either.left(throwableMapper.apply(throwable)), identity());
    }

    // --- SIMPLE Strategies --- //

    /**
     * Higher-order function to compose list of Simple validators into Fail-Fast Strategy.
     *
     * @param validations
     * @param invalidValidatable
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> validatable == null
                ? invalidValidatable
                : applySimpleValidations(validatable, validations.iterator(), throwableMapper)
                .filter(result -> result != none).getOrElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> validatable == null
                ? invalidValidatable
                : applySimpleValidations(validatable, Iterator.concat(toSimpleValidations(validationConfig, none), validations), throwableMapper)
                .filter(result -> result != none).getOrElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategyForHeader(
            List<SimpleValidator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> {
                if (validatable == null) {
                    return invalidValidatable;
                }
                val batch = validationConfig.getBatchMapper().apply(validatable);
                val batchSizeFailure = validateSize(batch, none, validationConfig);
                if (batchSizeFailure != none) {
                    return batchSizeFailure;
                }
                return applySimpleValidations(validatable, validations.iterator(), throwableMapper)
                    .filter(result -> result != none).getOrElse(none);
        };
    }

    /*static <FailureT, ValidatableT> SimpleFailFastStrategyForBatch<ValidatableT, FailureT> failFastStrategyForBatch(
            List<SimpleValidator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables -> {
            return validatables.map(failFastStrategy(validations, invalidValidatable, none, throwableMapper));
        };
    }*/
    
    /*private static <ValidatableT, FailureT> Iterator<Either<FailureT, ValidatableT>> filterDuplicates(
            List<ValidatableT> validatables, 
            Function1<ValidatableT, Object> groupingMapper, FailureT failure) {
        val groups = validatables.groupBy(groupingMapper);
        val partitions = groups.values().partition(group -> group.size() == 1);
        val duplicates = partitions._2.flatMap(identity()).map(duplicate -> Either.left(failure));
    } */

    private static <FailureT> FailureT validateSize(java.util.List<?> validatables,
                                                                  FailureT none,
                                                                  HeaderValidationConfig<?, FailureT> validationConfig) {
        if (validatables.size() < validationConfig.getMinBatchSize()._1) {
            return validationConfig.getMinBatchSize()._2;
        } else if (validatables.size() > validationConfig.getMaxBatchSize()._1) {
            return validationConfig.getMaxBatchSize()._2;
        }
        return none;
    }

    private static <ValidatableT, FailureT> Iterator<SimpleValidator<ValidatableT, FailureT>> toSimpleValidations(
            ValidationConfig<ValidatableT, FailureT> validationConfig, FailureT none) {
        Iterator<SimpleValidator<ValidatableT, FailureT>> mandatoryFieldValidations = validationConfig.getMandatoryFieldMappers().iterator()
                .map(tuple2 -> validatable -> isPresent.test(tuple2._1.apply(validatable)) ? none : tuple2._2);
        Iterator<SimpleValidator<ValidatableT, FailureT>> sfIdValidations = validationConfig.getSfIdFieldMappers().iterator()
                .map(tuple2 -> validatable -> IdTraits.isValidId(tuple2._1.apply(validatable).toString()) ? none : tuple2._2);
        return mandatoryFieldValidations.concat(sfIdValidations);
    }

    private static <FailureT, ValidatableT> Iterator<FailureT> applySimpleValidations(
            ValidatableT toBeValidated,
            Iterator<SimpleValidator<ValidatableT, FailureT>> validations,
            Function1<Throwable, FailureT> throwableMapper) {
        return validations.map(validation -> fireSimpleValidation(validation, toBeValidated, throwableMapper));
    }

    private static <FailureT, ValidatableT> FailureT fireSimpleValidation(
            SimpleValidator<ValidatableT, FailureT> validation,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return Try.of(() -> validation.apply(validatable)).fold(throwableMapper, identity());
    }

}

@FunctionalInterface
interface AccumulationStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, List<Either<FailureT, ValidatableT>>> {
}

@FunctionalInterface
interface FailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
}

@FunctionalInterface
interface FailFastStrategyForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, Either<FailureT, ValidatableT>> {
}

@FunctionalInterface
interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}

@FunctionalInterface
interface SimpleFailFastStrategyForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<FailureT>> {
}
