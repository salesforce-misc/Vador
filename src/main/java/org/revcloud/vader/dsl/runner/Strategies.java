/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import com.force.swag.id.IdTraits;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.revcloud.vader.dsl.runner.config.BaseValidationConfig;
import org.revcloud.vader.dsl.runner.config.BatchValidationConfig;
import org.revcloud.vader.dsl.runner.config.HeaderValidationConfig;
import org.revcloud.vader.dsl.runner.config.ValidationConfig;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.function.Predicate;

import static io.vavr.Function1.identity;

/**
 * These Strategies compose multiple validations and return a single function which can be applied on the Validatable.
 * <p>
 * TODO: Cleanup this class, this is too clumsy
 *
 * @author gakshintala
 * @since 228
 */
@Slf4j
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
            @NonNull List<Validator<ValidatableT, FailureT>> validations, // TODO: 26/03/21 validators vs validations naming consistency
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            val toBeValidatedRight = Either.<FailureT, ValidatableT>right(toBeValidated);
            return fireValidations(toBeValidatedRight, validations.iterator(), throwableMapper)
                    .find(Either::isLeft)
                    .getOrElse(toBeValidatedRight); // TODO: 26/03/21 do either-wrapper here and not in applyValidations, so that it can be reused here
        };
    }

    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            @NonNull List<Validator<ValidatableT, FailureT>> validations,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            val toBeValidatedRight = Either.<FailureT, ValidatableT>right(toBeValidated);
            return fireValidations(toBeValidatedRight, Iterator.concat(toValidations(validationConfig), validations), throwableMapper)
                    .find(Either::isLeft)
                    .getOrElse(toBeValidatedRight);
        };
    }

    private static <ValidatableT, FailureT> Iterator<Validator<ValidatableT, FailureT>> toValidations(
            BaseValidationConfig<ValidatableT, FailureT> validationConfig) {
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
                : fireValidations(Either.right(toBeValidated), validations.iterator(), throwableMapper).toList();
    }

    private static <FailureT, ValidatableT> Iterator<Either<FailureT, ValidatableT>> fireValidations(
            Either<FailureT, ValidatableT> toBeValidatedRight, // TODO: 28/03/21 toBeValidated vs Validatable naming consistency
            Iterator<Validator<ValidatableT, FailureT>> validations,
            Function1<Throwable, FailureT> throwableMapper) {
        return validations
                .map(currentValidation -> fireValidation(currentValidation, toBeValidatedRight, throwableMapper));
    }

    private static <FailureT, ValidatableT> Either<FailureT, ValidatableT> fireValidation(
            Validator<ValidatableT, FailureT> validation,
            Either<FailureT, ValidatableT> toBeValidatedRight,
            Function1<Throwable, FailureT> throwableMapper) {
        return Try.of(() -> validation.apply(toBeValidatedRight))
                .fold(throwable -> Either.left(throwableMapper.apply(throwable)), Function1.identity())
                .flatMap(ignore -> toBeValidatedRight); // Put the original Validatable in the right state
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

    static <FailureT, ValidatableT> FailFastStrategyForBatch<ValidatableT, FailureT> failFastStrategyForBatch(
            List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables -> {
            final Seq<Either<FailureT, ValidatableT>> validatablesEither =
                    filterInvalidatablesAndDuplicates(validatables, invalidValidatable, validationConfig);
            return validatablesEither.map(validatableEither -> Iterator.concat(toValidations(validationConfig), validators).iterator()
                    .map(currentValidation -> fireValidation(currentValidation, validatableEither, throwableMapper))
                    .find(Either::isLeft)
                    .getOrElse(validatableEither)).toList();
        };
    }

    static <ValidatableT, FailureT> Seq<Either<FailureT, ValidatableT>> filterInvalidatablesAndDuplicates(
            List<ValidatableT> validatables,
            FailureT invalidValidatable,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        if (validatables.isEmpty()) {
            return List.empty();
        } else if (validatables.size() == 1) {
            val validatable = validatables.get(0);
            return validatable == null ? List.of(Either.left(invalidValidatable)) : List.of(Either.right(validatables.get(0)));
        }
        final var filterDuplicatesConfig = batchValidationConfig.getFilterDuplicates();
        val keyMapperForDuplicates = filterDuplicatesConfig == null
                ? Function1.<ValidatableT>identity()
                : filterDuplicatesConfig._2;
        val groups = validatables.zipWithIndex()
                .groupBy(tuple2 -> tuple2._1 == null ? null : keyMapperForDuplicates.apply(tuple2._1));

        groups.forEach(group -> log.info(group.toString()));
        Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> invalidValidatables = groups.get(null)
                .map(nullValidatables -> invalidate(nullValidatables, invalidValidatable))
                .getOrElse(List.empty());

        val partition = groups.remove(null).values().partition(group -> group.size() == 1);
        val failureForDuplicate = batchValidationConfig.getFilterDuplicates()._1;
        Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> duplicates =
                partition._2.flatMap(identity()).map(duplicate -> Tuple.of(Either.left(failureForDuplicate), duplicate._2));
        Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> nonDuplicates =
                partition._1.flatMap(identity()).map(tuple2 -> tuple2.map1(Either::right));

        return duplicates.appendAll(nonDuplicates).appendAll(invalidValidatables).sortBy(Tuple2::_2).map(Tuple2::_1);
    }

    private static <FailureT, ValidatableT> Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> invalidate(
            Seq<Tuple2<ValidatableT, Integer>> nullValidatables, FailureT invalidValidatable) {
        return nullValidatables.map(nullValidatable -> nullValidatable.map1(ignore -> Either.left(invalidValidatable)));
    }

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
interface FailFastStrategyForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>> {
}

@FunctionalInterface
interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
}
