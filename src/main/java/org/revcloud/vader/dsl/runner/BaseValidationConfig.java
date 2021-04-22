package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.revcloud.vader.dsl.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.dsl.runner.SpecFactory.BaseSpec.BaseSpecBuilder;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.revcloud.vader.dsl.lift.ValidatorLiftDsl.liftAllSimple;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ?>, FailureT>> shouldHaveFields;
    protected Tuple2<Map<String, Function1<ValidatableT, ?>>, Function2<String, Object, FailureT>> shouldHaveFieldsWithName;
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ID>, FailureT>> shouldHaveValidSFIds;
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ID>, FailureT>> mayHaveValidSFIds;
    protected Function1<SpecFactory<ValidatableT, FailureT>, Collection<? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> withSpecs;
    @Singular("withSpec")
    protected Collection<Function1<SpecFactory<ValidatableT, FailureT>, ? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>> withSpec;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Builder.Default
    Tuple2<Collection<SimpleValidator<ValidatableT, FailureT>>, FailureT> withSimpleValidators = Tuple.of(Collections.emptyList(), null);
    @Singular("withSimpleValidator")
    Collection<Tuple2<SimpleValidator<ValidatableT, FailureT>, FailureT>> withSimpleValidator;

    Stream<Validator<ValidatableT, FailureT>> getValidatorsStream() {
        var simpleValidators = Stream.concat(withSimpleValidators._1.stream(), withSimpleValidator.stream().map(Tuple2::_1)).collect(Collectors.toList());
        return Stream.concat(withValidators.stream(), liftAllSimple(List.ofAll(simpleValidators), withSimpleValidators._2).toJavaStream());
    }

    Stream<BaseSpec<ValidatableT, FailureT>> getSpecsStream() {
        val specFactory = new SpecFactory<ValidatableT, FailureT>();
        return Stream.concat(Stream.ofNullable(withSpecs).flatMap(specs -> specs.apply(specFactory).stream().map(BaseSpecBuilder::done)),
                Stream.ofNullable(withSpec).flatMap(specs -> specs.stream().map(spec -> spec.apply(specFactory).done())));
    }

    public Optional<Predicate<ValidatableT>> getSpecWithName(@NonNull String nameForTest) {
        return getSpecsStream().filter(spec -> nameForTest.equals(spec.nameForTest)).findFirst().map(BaseSpec::toPredicate);
    }
}
