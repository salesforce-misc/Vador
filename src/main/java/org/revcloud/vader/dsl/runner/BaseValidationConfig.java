package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.revcloud.vader.dsl.runner.BaseSpec.BaseSpecBuilder;
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
    @Singular
    protected Collection<BaseSpecBuilder<ValidatableT, FailureT, ?, ?>> withSpecBuilders;
    @Singular
    protected Collection<BaseSpec<ValidatableT, FailureT>> withSpecs;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Builder.Default
    Tuple2<Collection<SimpleValidator<ValidatableT, FailureT>>, FailureT> withSimpleValidators = Tuple.of(Collections.emptyList(), null);
    Tuple2<SimpleValidator<ValidatableT, FailureT>, FailureT> withSimpleValidator;

    Stream<Validator<ValidatableT, FailureT>> getValidators() {
        if (withSimpleValidator != null) {
            withSimpleValidators._1.add(withSimpleValidator._1);
        }
        return Iterator.concat(withValidators, liftAllSimple(List.ofAll(withSimpleValidators._1), withSimpleValidators._2)).toJavaStream();
    }
}
