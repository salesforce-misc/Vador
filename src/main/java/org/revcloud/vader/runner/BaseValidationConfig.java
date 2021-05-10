package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.SpecFactory.BaseSpec.BaseSpecBuilder;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.revcloud.vader.lift.ValidatorLiftUtil.liftAllSimple;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
    @Singular("shouldHaveFieldOrFailWith")
    protected Map<TypedPropertyGetter<ValidatableT, ?>, FailureT> shouldHaveFieldsOrFailWith;
    @Nullable
    protected Tuple2<Collection<TypedPropertyGetter<ValidatableT, ?>>, Function2<String, Object, FailureT>> shouldHaveFieldsOrFailWithFn;
    @Singular("shouldHaveValidSFIdFieldOrFailWith")
    protected Map<TypedPropertyGetter<ValidatableT, ID>, FailureT> shouldHaveValidSFIdFormatOrFailWith;
    @Nullable
    protected Tuple2<Collection<TypedPropertyGetter<ValidatableT, ID>>, @NonNull Function2<String, ID, FailureT>> shouldHaveValidSFIdFormatOrFailWithFn;
    @Singular("mayHaveValidSFIdFieldOrFailWith")
    protected Map<TypedPropertyGetter<ValidatableT, ID>, FailureT> absentOrHaveValidSFIdFieldsOrFailWith;
    @Nullable
    protected Tuple2<Collection<TypedPropertyGetter<ValidatableT, ID>>, Function2<String, ID, FailureT>> absentOrHaveValidSFIdFormatOrFailWithFn;
    protected Function1<SpecFactory<ValidatableT, FailureT>, Collection<? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> withSpecs;
    @Singular("withSpec")
    protected Collection<Function1<SpecFactory<ValidatableT, FailureT>, ? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> withSpec;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Builder.Default
    Tuple2<Collection<SimpleValidator<ValidatableT, FailureT>>, FailureT> withSimpleValidatorsOrFailWith = Tuple.of(Collections.emptyList(), null);
    @Singular("withSimpleValidatorOrFailWith")
    Collection<Tuple2<SimpleValidator<ValidatableT, FailureT>, FailureT>> withSimpleValidators;

    Stream<Validator<ValidatableT, FailureT>> getValidatorsStream() {
        var simpleValidators = Stream.concat(withSimpleValidatorsOrFailWith._1.stream(), withSimpleValidators.stream().map(Tuple2::_1)).collect(Collectors.toList());
        return Stream.concat(withValidators.stream(), liftAllSimple(simpleValidators, withSimpleValidatorsOrFailWith._2).stream());
    }

    Stream<SpecFactory.BaseSpec<ValidatableT, FailureT>> getSpecsStream() {
        val specFactory = new SpecFactory<ValidatableT, FailureT>();
        return Stream.concat(Stream.ofNullable(withSpecs).flatMap(specs -> specs.apply(specFactory).stream().map(BaseSpecBuilder::done)),
                Stream.ofNullable(withSpec).flatMap(specs -> specs.stream().map(spec -> spec.apply(specFactory).done())));
    }

    public Optional<Predicate<ValidatableT>> getSpecWithName(@NonNull String nameForTest) {
        // TODO 29/04/21 gopala.akshintala: Move this duplicate check to prepare 
        val specNameToSpecs = getSpecsStream().collect(Collectors.groupingBy(SpecFactory.BaseSpec::getNameForTest));
        val duplicateSpecNames = specNameToSpecs.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        if (!duplicateSpecNames.isEmpty()) {
            throw new IllegalArgumentException("Duplicate Spec Names found" + String.join(",", duplicateSpecNames));
        }
        return getSpecsStream().filter(spec -> nameForTest.equals(spec.nameForTest)).findFirst().map(SpecFactory.BaseSpec::toPredicate);
    }

    public Set<String> getRequiredFieldNames(Class<ValidatableT> beanClass) {
        return Stream.concat(
                Stream.ofNullable(shouldHaveFieldsOrFailWith).flatMap(f -> f.keySet().stream()),
                Stream.ofNullable(shouldHaveFieldsOrFailWithFn).flatMap(f -> f._1.stream()))
                .map(fieldMapper -> PropertyUtils.getPropertyName(beanClass, fieldMapper)).collect(Collectors.toSet());
    }

    public Set<String> getRequiredFieldNamesForSFIdFormat(Class<ValidatableT> beanClass) {
        return Stream.concat(
                Stream.ofNullable(shouldHaveValidSFIdFormatOrFailWith).flatMap(f -> f.keySet().stream()),
                Stream.ofNullable(shouldHaveValidSFIdFormatOrFailWithFn).flatMap(f -> f._1.stream()))
                .map(fieldMapper -> PropertyUtils.getPropertyName(beanClass, fieldMapper)).collect(Collectors.toSet());
    }

    public Set<String> getNonRequiredFieldNamesForSFIdFormat(Class<ValidatableT> beanClass) {
        return Stream.concat(
                Stream.ofNullable(absentOrHaveValidSFIdFieldsOrFailWith).flatMap(f -> f.keySet().stream()),
                Stream.ofNullable(absentOrHaveValidSFIdFormatOrFailWithFn).flatMap(f -> f._1.stream()))
                .map(fieldMapper -> PropertyUtils.getPropertyName(beanClass, fieldMapper)).collect(Collectors.toSet());
    }
}
