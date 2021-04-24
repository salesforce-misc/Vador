package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.revcloud.vader.dsl.lift.ValidatorLiftDsl.liftAllSimple;

@Value
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<ValidatableT, FailureT> {
    @Builder.Default
    Tuple2<Integer, FailureT> minBatchSize = Tuple.of(Integer.MIN_VALUE, null);
    @Builder.Default
    Tuple2<Integer, FailureT> maxBatchSize = Tuple.of(Integer.MAX_VALUE, null);
    @NonNull
    Function1<ValidatableT, Collection<?>> withBatchMapper;
    @Singular
    Collection<Validator<ValidatableT, FailureT>> withValidators;
    @Builder.Default
    Tuple2<Collection<SimpleValidator<ValidatableT, FailureT>>, FailureT> withSimpleValidatorsOrFailWith = Tuple.of(Collections.emptyList(), null);
    @Singular("withSimpleValidator")
    Collection<Tuple2<SimpleValidator<ValidatableT, FailureT>, FailureT>> withSimpleValidators;

    Stream<Validator<ValidatableT, FailureT>> getValidatorsStream() {
        var simpleValidators = Stream.concat(withSimpleValidatorsOrFailWith._1.stream(), withSimpleValidators.stream().map(Tuple2::_1)).collect(Collectors.toList());
        return Stream.concat(withValidators.stream(), liftAllSimple(io.vavr.collection.List.ofAll(simpleValidators), withSimpleValidatorsOrFailWith._2).toJavaStream());
    }
}
