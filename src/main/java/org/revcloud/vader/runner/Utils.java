package org.revcloud.vader.runner;

import com.force.swag.id.IdTraits;
import de.cronn.reflection.util.PropertyUtils;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.types.validators.Validator;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.vavr.CheckedFunction1.liftTry;
import static io.vavr.Function1.identity;

@UtilityClass
class Utils {
    static <FailureT, ValidatableT> Stream<Either<FailureT, ValidatableT>> fireValidators(
            Either<FailureT, ValidatableT> validatable,
            Stream<Validator<ValidatableT, FailureT>> validators,
            Function1<Throwable, FailureT> throwableMapper) {
        return validators
                .map(currentValidator -> fireValidator(currentValidator, validatable, throwableMapper));
    }

    static <FailureT, ValidatableT> Either<FailureT, ValidatableT> fireValidator(
            Validator<ValidatableT, FailureT> validator,
            Either<FailureT, ValidatableT> toBeValidatedRight,
            Function1<Throwable, FailureT> throwableMapper) {
        return liftTry(validator).apply(toBeValidatedRight)
                .fold(throwable -> Either.left(throwableMapper.apply(throwable)), Function1.identity())
                .flatMap(ignore -> toBeValidatedRight); // Put the original Validatable in the right state
    }

    static <FailureT> Optional<FailureT> validateSize(java.util.Collection<?> validatables,
                                                    HeaderValidationConfig<?, FailureT> headerConfig) {
        val minBatchSize = headerConfig.getMinBatchSize();
        if (minBatchSize != null && validatables.size() < minBatchSize._1) {
            return Optional.of(minBatchSize._2);
        }
        val maxBatchSize = headerConfig.getMaxBatchSize();
        if (maxBatchSize != null && validatables.size() > maxBatchSize._1) {
            return Optional.of(maxBatchSize._2);
        }
        return Optional.empty();
    }

    static <ValidatableT, FailureT> Seq<Either<FailureT, ValidatableT>> filterInvalidatablesAndDuplicates(
            java.util.List<ValidatableT> validatables,
            FailureT invalidValidatable,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        if (validatables.isEmpty()) {
            return List.empty();
        } else if (validatables.size() == 1) {
            val validatable = validatables.get(0);
            return validatable == null ? List.of(Either.left(invalidValidatable)) : List.of(Either.right(validatables.get(0)));
        }
        val duplicateFinder = batchValidationConfig.getFindDuplicatesWith();
        val keyMapperForDuplicates = duplicateFinder == null ? Function1.<ValidatableT>identity() : duplicateFinder;
        val groups = List.ofAll(validatables).zipWithIndex() // groups: invalids, duplicates, non-duplicates
                .groupBy(tuple2 -> tuple2._1 == null ? null : keyMapperForDuplicates.apply(tuple2._1));

        val invalids = groups.get(null)
                .map(nullValidatables -> invalidate(nullValidatables, invalidValidatable))
                .getOrElse(List.empty());

        // TODO 11/04/21 gopala.akshintala: add test 
        if (duplicateFinder == null) { // Skip the rest if duplicateFinder is not defined
            final Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> valids =
                    groups.remove(null).values().flatMap(identity()).map(tuple2 -> tuple2.map1(Either::right));
            return valids.appendAll(invalids).sortBy(Tuple2::_2).map(Tuple2::_1);
        }

        val partition = groups.remove(null).values().partition(group -> group.size() == 1);
        val failureForDuplicate = batchValidationConfig.getAndFailDuplicatesWith();
        Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> duplicates = (failureForDuplicate == null) ? List.empty()
                : partition._2.flatMap(identity()).map(duplicate -> Tuple.of(Either.left(failureForDuplicate), duplicate._2));
        val nonDuplicates = partition._1.flatMap(identity()).map(tuple2 -> tuple2.map1(Either::<FailureT, ValidatableT>right));
        return nonDuplicates.appendAll(duplicates).appendAll(invalids).sortBy(Tuple2::_2).map(Tuple2::_1);
    }

    static <ValidatableT, FailureT> Optional<FailureT> filterInvalidatablesAndDuplicatesForAllOrNone(
            java.util.List<ValidatableT> validatables,
            FailureT invalidValidatable,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        if (validatables.isEmpty()) {
            return Optional.empty();
        } else if (validatables.size() == 1) {
            val validatable = validatables.get(0);
            return validatable == null ? Optional.of(invalidValidatable) : Optional.empty();
        }
        val duplicateFinder = batchValidationConfig.getFindDuplicatesWith();
        val keyMapperForDuplicates = duplicateFinder == null ? Function1.<ValidatableT>identity() : duplicateFinder;
        val groups = List.ofAll(validatables) // groups: invalids, duplicates, non-duplicates
                .groupBy(validatable -> validatable == null ? null : keyMapperForDuplicates.apply(validatable));

        val invalids = groups.get(null);
        if (invalids.isDefined() && !invalids.get().isEmpty()) {
            return Optional.of(invalidValidatable);
        }

        val valids = groups.remove(null).values();
        val failureForDuplicate = batchValidationConfig.getAndFailDuplicatesWith();
        if (duplicateFinder != null && failureForDuplicate != null) {
            val partition = valids.partition(group -> group.size() == 1);
            if (!partition._2.isEmpty()) {
                return Optional.of(failureForDuplicate);
            }
        }
        return Optional.empty();
    }

    private static <FailureT, ValidatableT> Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> invalidate(
            Seq<Tuple2<ValidatableT, Integer>> nullValidatables, FailureT invalidValidatable) {
        return nullValidatables.map(nullValidatable -> nullValidatable.map1(ignore -> Either.left(invalidValidatable)));
    }

    static <ValidatableT, FailureT> Stream<Validator<ValidatableT, FailureT>> toValidators(
            BaseValidationConfig<ValidatableT, FailureT> validationConfig) {
        Stream<Validator<ValidatableT, FailureT>> mandatoryFieldValidators1 = validationConfig.getShouldHaveFieldsOrFailWith().entrySet().stream()
                .map(entry -> validatableRight -> validatableRight.map(entry.getKey()::get).filterOrElse(isPresent, ignore -> entry.getValue()));
        Stream<Validator<ValidatableT, FailureT>> mandatoryFieldValidators2 = Stream.ofNullable(validationConfig.getShouldHaveFieldsOrFailWithFn())
                .flatMap(tuple2 -> tuple2._1.stream())
                .map(fieldMapper -> validatableRight -> validatableRight.map(fieldMapper::get)
                        .filterOrElse(isPresent, fieldValue -> validationConfig.getShouldHaveFieldsOrFailWithFn()._2.apply(PropertyUtils.getPropertyName(validatableRight.get(), fieldMapper), fieldValue)));
        Stream<Validator<ValidatableT, FailureT>> mandatorySfIdValidators1 = validationConfig.getShouldHaveValidSFIdFieldsOrFailWith().entrySet().stream()
                .map(entry -> validatableRight -> validatableRight.map(entry.getKey()::get)
                        .filterOrElse(id -> id != null && IdTraits.isValidId(id.toString()), ignore -> entry.getValue()));
        Stream<Validator<ValidatableT, FailureT>> mandatorySfIdValidators2 = Stream.ofNullable(validationConfig.getShouldHaveValidSFIdFieldsOrFailWithFn())
                .flatMap(tuple2 -> tuple2._1.stream())
                .map(fieldMapper -> validatableRight -> validatableRight.map(fieldMapper::get)
                        .filterOrElse(id -> id != null && IdTraits.isValidId(id.toString()), id -> validationConfig.getShouldHaveValidSFIdFieldsOrFailWithFn()._2.apply(PropertyUtils.getPropertyName(validatableRight.get(), fieldMapper), id)));
        Stream<Validator<ValidatableT, FailureT>> nonMandatorySfIdValidators1 = validationConfig.getMayHaveValidSFIdFieldsOrFailWith().entrySet().stream()
                .map(entry -> validatableRight -> validatableRight.map(entry.getKey()::get)
                        .filterOrElse(id -> id == null || IdTraits.isValidId(id.toString()), ignore -> entry.getValue()));
        Stream<Validator<ValidatableT, FailureT>> nonMandatorySfIdValidators2 = Stream.ofNullable(validationConfig.getShouldHaveValidSFIdFieldsOrFailWithFn())
                .flatMap(tuple2 -> tuple2._1.stream())
                .map(fieldMapper -> validatableRight -> validatableRight.map(fieldMapper::get)
                        .filterOrElse(id -> id == null || IdTraits.isValidId(id.toString()), id -> validationConfig.getShouldHaveValidSFIdFieldsOrFailWithFn()._2.apply(PropertyUtils.getPropertyName(validatableRight.get(), fieldMapper), id)));

        val specValidators = validationConfig.getSpecsStream().map(Utils::toValidator);
        return Stream.of(mandatoryFieldValidators1, mandatoryFieldValidators2, mandatorySfIdValidators1, mandatorySfIdValidators2, nonMandatorySfIdValidators1, nonMandatorySfIdValidators2, specValidators, validationConfig.getValidatorsStream())
                .flatMap(identity());
    }

    private static <ValidatableT, FailureT> Validator<ValidatableT, FailureT> toValidator(SpecFactory.BaseSpec<ValidatableT, FailureT> baseSpec) {
        return validatableRight -> validatableRight.filterOrElse(baseSpec.toPredicate(), baseSpec::getFailure);
    }

    private static final Predicate<Object> isPresent = fieldValue -> {
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                return !((String) fieldValue).isBlank();
            }
            return true;
        }
        return false;
    };

    static <FailureT, ValidatableT> Either<FailureT, ValidatableT> findFirstFailure(
            Either<FailureT, ValidatableT> validatable,
            BaseValidationConfig<ValidatableT, FailureT> validationConfig,
            Function1<Throwable, FailureT> throwableMapper) {
        if (validatable.isLeft()) {
            return validatable;
        }
        return fireValidators(validatable, toValidators(validationConfig), throwableMapper)
                .filter(Either::isLeft)
                .findFirst()
                .orElse(validatable);
    }
}
