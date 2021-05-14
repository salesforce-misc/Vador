package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.runner.SpecFactory.BaseSpec.BaseSpecBuilder;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
    @Singular("shouldHaveFieldOrFailWith")
    protected Map<@NonNull TypedPropertyGetter<ValidatableT, ?>, FailureT> shouldHaveFieldsOrFailWith;
    @Nullable
    protected Tuple2<@NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ?>>, @NonNull Function2<String, Object, FailureT>> shouldHaveFieldsOrFailWithFn;
    @Singular("shouldHaveValidSFIdFieldOrFailWith")
    protected Map<@NonNull TypedPropertyGetter<ValidatableT, ID>, FailureT> shouldHaveValidSFIdFormatOrFailWith;
    @Nullable
    protected Tuple2<@NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>, @NonNull Function2<String, ID, FailureT>> shouldHaveValidSFIdFormatOrFailWithFn;
    @Singular("mayHaveValidSFIdFieldOrFailWith")
    protected Map<@NonNull TypedPropertyGetter<ValidatableT, ID>, FailureT> absentOrHaveValidSFIdFieldsOrFailWith;
    @Nullable
    protected Tuple2<@NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>, @NonNull Function2<String, ID, FailureT>> absentOrHaveValidSFIdFormatOrFailWithFn;
    @Nullable
    protected Function1<SpecFactory<ValidatableT, FailureT>, Collection<? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> specify;
    @Singular("withSpec")
    protected Collection<Function1<SpecFactory<ValidatableT, FailureT>, ? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> withSpecs;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Nullable
    Tuple2<@NonNull Collection<? extends SimpleValidator<? super ValidatableT, FailureT>>, @NonNull FailureT> withSimpleValidators;
    @Singular("withSimpleValidator")
    Collection<Tuple2<@NonNull ? extends SimpleValidator<? super ValidatableT, FailureT>, @NonNull FailureT>> withSimpleValidator;

    Stream<BaseSpec<ValidatableT, FailureT>> getSpecsStream() {
        val specFactory = new SpecFactory<ValidatableT, FailureT>();
        return Stream.concat(Stream.ofNullable(specify).flatMap(specs -> specs.apply(specFactory).stream().map(BaseSpecBuilder::done)),
                Stream.ofNullable(withSpecs).flatMap(specs -> specs.stream().map(spec -> spec.apply(specFactory).done())));
    }

    public Optional<Predicate<ValidatableT>> getSpecWithName(@NonNull String nameForTest) {
        // TODO 29/04/21 gopala.akshintala: Move this duplicate check to prepare 
        val specNameToSpecs = getSpecsStream().collect(Collectors.groupingBy(BaseSpec::getNameForTest));
        val duplicateSpecNames = specNameToSpecs.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        if (!duplicateSpecNames.isEmpty()) {
            throw new IllegalArgumentException("Duplicate Spec Names found" + String.join(",", duplicateSpecNames));
        }
        return getSpecsStream().filter(spec -> nameForTest.equals(spec.nameForTest)).findFirst().map(BaseSpec::toPredicate);
    }

    public Set<String> getRequiredFieldNames(Class<ValidatableT> beanClass) {
        return Stream.concat(
                Stream.of(shouldHaveFieldsOrFailWith).flatMap(f -> f.keySet().stream()),
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
