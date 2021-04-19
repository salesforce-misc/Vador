package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.revcloud.vader.dsl.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.revcloud.vader.dsl.lift.ValidatorLiftDsl.liftAllSimple;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ?>, FailureT>> shouldHaveFields;
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ID>, FailureT>> shouldHaveValidSFIds;
    @Singular
    protected Collection<Tuple2<Function1<ValidatableT, ID>, FailureT>> mayHaveValidSFIds;
    protected Function1<SpecFactory<ValidatableT, FailureT>, Collection<? extends BaseSpec<ValidatableT, FailureT>>> withSpecs;
    protected Function1<SpecFactory<ValidatableT, FailureT>, ? extends BaseSpec<ValidatableT, FailureT>> withSpec;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Builder.Default
    Tuple2<Collection<SimpleValidator<ValidatableT, FailureT>>, FailureT> withSimpleValidators = Tuple.of(Collections.emptyList(), null);
    Tuple2<SimpleValidator<ValidatableT, FailureT>, FailureT> withSimpleValidator;

    Stream<Validator<ValidatableT, FailureT>> getValidatorsStream() {
        var simpleValidators = List.ofAll(withSimpleValidators._1);
        if (withSimpleValidator != null) {
            simpleValidators = simpleValidators.append(withSimpleValidator._1);
        }
        return Stream.concat(withValidators.stream(), liftAllSimple(simpleValidators, withSimpleValidators._2).toJavaStream());
    }

    Stream<BaseSpec<ValidatableT, FailureT>> getSpecsStream() {
        final var specFactory = new SpecFactory<ValidatableT, FailureT>();
        return Stream.concat(Stream.ofNullable(withSpecs).flatMap(specs -> specs.apply(specFactory).stream()), 
                Stream.ofNullable(withSpec).map(spec -> spec.apply(specFactory)));
    }
}
