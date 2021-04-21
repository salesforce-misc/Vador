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
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.dsl.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.CheckedFunction1.liftTry;
import static io.vavr.Function1.identity;

@UtilityClass
// TODO 20/04/21 gopala.akshintala: Split this class 
class Utils {
    static <FailureT, ValidatableT> Iterator<Either<FailureT, ValidatableT>> fireValidators(
            Either<FailureT, ValidatableT> toBeValidatedRight, // TODO: 28/03/21 toBeValidated vs Validatable naming consistency
            Iterator<Validator<ValidatableT, FailureT>> validators,
            Function1<Throwable, FailureT> throwableMapper) {
        return validators
                .map(currentValidator -> fireValidator(currentValidator, toBeValidatedRight, throwableMapper));
    }

    static <FailureT, ValidatableT> Either<FailureT, ValidatableT> fireValidator(
            Validator<ValidatableT, FailureT> validator,
            Either<FailureT, ValidatableT> toBeValidatedRight,
            Function1<Throwable, FailureT> throwableMapper) {
        return liftTry(validator).apply(toBeValidatedRight)
                .fold(throwable -> Either.left(throwableMapper.apply(throwable)), Function1.identity())
                .flatMap(ignore -> toBeValidatedRight); // Put the original Validatable in the right state
    }

    static <FailureT> FailureT validateSize(java.util.List<?> validatables,
                                            FailureT none,
                                            HeaderValidationConfig<?, FailureT> headerConfig) {
        val minBatchSize = headerConfig.getMinBatchSize();
        if (minBatchSize != null && validatables.size() < minBatchSize._1) {
            return minBatchSize._2;
        }
        val maxBatchSize = headerConfig.getMaxBatchSize();
        if (maxBatchSize != null && validatables.size() > maxBatchSize._1) {
            return maxBatchSize._2;
        }
        return none;
    }

    static <FailureT, ValidatableT> Iterator<FailureT> applySimpleValidators(
            ValidatableT toBeValidated,
            Iterator<SimpleValidator<ValidatableT, FailureT>> validators,
            Function1<Throwable, FailureT> throwableMapper) {
        return validators.map(validator -> fireSimpleValidator(validator, toBeValidated, throwableMapper));
    }

    private static <FailureT, ValidatableT> FailureT fireSimpleValidator(
            SimpleValidator<ValidatableT, FailureT> validator,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return Try.of(() -> validator.apply(validatable)).fold(throwableMapper, identity());
    }

    static <ValidatableT, FailureT> boolean matchFields(BaseSpec<ValidatableT, FailureT> baseSpec, ValidatableT validatable, Object actualValue) {
        val expectedFieldMappers = new ArrayList<>(baseSpec.getOrMatchesFields());
        if (baseSpec.getMatchesField() != null) {
            expectedFieldMappers.add(baseSpec.getMatchesField());
        }
        return expectedFieldMappers.stream()
                .anyMatch(expectedFieldMapper -> expectedFieldMapper.apply(validatable) == actualValue);
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
        val duplicateFinder = batchValidationConfig.getFindDuplicatesWith();
        val keyMapperForDuplicates = duplicateFinder == null ? Function1.<ValidatableT>identity() : duplicateFinder;
        val groups = validatables.zipWithIndex()
                .groupBy(tuple2 -> tuple2._1 == null ? null : keyMapperForDuplicates.apply(tuple2._1));

        Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> invalidValidatables = groups.get(null)
                .map(nullValidatables -> invalidate(nullValidatables, invalidValidatable))
                .getOrElse(List.empty());
        
        // TODO 11/04/21 gopala.akshintala: refactor 
        if (duplicateFinder == null) {
            final Seq<Tuple2<Either<FailureT, ValidatableT>, Integer>> map =
                    groups.remove(null).values().flatMap(identity()).map(tuple2 -> tuple2.map1(Either::right));
            return map.appendAll(invalidValidatables).sortBy(Tuple2::_2).map(Tuple2::_1);
        }
        val partition = groups.remove(null).values().partition(group -> group.size() == 1);
        val failureForDuplicate = batchValidationConfig.getAndFailDuplicatesWith();
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

    static <ValidatableT, FailureT> Iterator<Validator<ValidatableT, FailureT>> toValidators(
            BaseValidationConfig<ValidatableT, FailureT> validationConfig) {
        Stream<Validator<ValidatableT, FailureT>> mandatoryFieldValidators = validationConfig.getShouldHaveFields().stream()
                .map(tuple2 -> validatableRight -> validatableRight.map(tuple2._1).filterOrElse(isPresent, ignore -> tuple2._2));
        Stream<Validator<ValidatableT, FailureT>> mandatorySfIdValidators = validationConfig.getShouldHaveValidSFIds().stream()
                .map(tuple2 -> validatableRight -> validatableRight.map(tuple2._1).map(ID::toString)
                        .filterOrElse(IdTraits::isValidId, ignore -> tuple2._2));
        Stream<Validator<ValidatableT, FailureT>> nonMandatorySfIdValidators = validationConfig.getMayHaveValidSFIds().stream()
                .map(tuple2 -> validatableRight -> validatableRight.map(tuple2._1).map(ID::toString)
                        .filter(Objects::nonNull) // Ignore if null
                        .fold(() -> validatableRight, id -> id.filterOrElse(IdTraits::isValidId, ignore -> tuple2._2)));
        Stream<Validator<ValidatableT, FailureT>> specValidators = validationConfig.getSpecsStream()
                .map(Utils::toValidator);
        // TODO 13/04/21 gopala.akshintala: Use Stream everywhere, now that java has immutable list built-in 
        return Iterator.ofAll(Stream.of(mandatoryFieldValidators, mandatorySfIdValidators, nonMandatorySfIdValidators, specValidators, validationConfig.getValidatorsStream())
                .flatMap(identity()).collect(Collectors.toList()));
    }


    private static <ValidatableT, FailureT> Validator<ValidatableT, FailureT> toValidator(BaseSpec<ValidatableT, FailureT> baseSpec) {
        return validatableRight -> validatableRight
                .filterOrElse(baseSpec.toPredicate(), baseSpec::getFailure);
    }

    static <ValidatableT, FailureT> Iterator<SimpleValidator<ValidatableT, FailureT>> toSimpleValidators(
            ValidationConfig<ValidatableT, FailureT> validationConfig, FailureT none) {
        Stream<SimpleValidator<ValidatableT, FailureT>> mandatoryFieldValidators = validationConfig.getShouldHaveFields().stream()
                .map(tuple2 -> validatable -> isPresent.test(tuple2._1.apply(validatable)) ? none : tuple2._2);
        Stream<SimpleValidator<ValidatableT, FailureT>> mandatorySfIdValidators = validationConfig.getShouldHaveValidSFIds().stream()
                .map(tuple2 -> validatable -> IdTraits.isValidId(tuple2._1.apply(validatable).toString()) ? none : tuple2._2);
        Stream<SimpleValidator<ValidatableT, FailureT>> nonMandatorySfIdValidators = validationConfig.getMayHaveValidSFIds().stream()
                .map(tuple2 -> validatable -> {
                    val idValue = tuple2._1.apply(validatable);
                    return idValue == null || IdTraits.isValidId(idValue.toString()) ? none : tuple2._2;
                });
        Stream<SimpleValidator<ValidatableT, FailureT>> specValidators = validationConfig.getSpecsStream()
                .map(specBuilder -> toSimpleValidator(specBuilder, none));
        // TODO 13/04/21 gopala.akshintala: Use Stream everywhere, now that java has immutable list built-in 
        return Iterator.ofAll(Stream.of(mandatoryFieldValidators, mandatorySfIdValidators, nonMandatorySfIdValidators, specValidators)
                .flatMap(identity()).collect(Collectors.toList()));
    }

    private static <ValidatableT, FailureT> SimpleValidator<ValidatableT, FailureT> toSimpleValidator(BaseSpec<ValidatableT, FailureT> baseSpec, FailureT none) {
        return validatable -> baseSpec.toPredicate().test(validatable) ? none : baseSpec.getFailure(validatable);
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
                .find(Either::isLeft)
                .getOrElse(validatable);
    }
}
