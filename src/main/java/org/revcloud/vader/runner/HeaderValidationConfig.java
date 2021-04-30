package org.revcloud.vader.runner;

import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.revcloud.vader.lift.ValidatorLiftUtil;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

@Value
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<HeaderValidatableT, FailureT> {
    @Builder.Default
    Tuple2<Integer, FailureT> minBatchSize = Tuple.of(Integer.MIN_VALUE, null);
    @Builder.Default
    Tuple2<Integer, FailureT> maxBatchSize = Tuple.of(Integer.MAX_VALUE, null);
    @NonNull
    TypedPropertyGetter<HeaderValidatableT, Collection<?>> withBatchMapper;
    @Singular
    Collection<Validator<HeaderValidatableT, FailureT>> withValidators;
    Tuple2<Collection<SimpleValidator<HeaderValidatableT, FailureT>>, FailureT> withSimpleValidatorsOrFailWith;
    @Singular("withSimpleValidator")
    Collection<Tuple2<SimpleValidator<HeaderValidatableT, FailureT>, FailureT>> withSimpleValidators;

    Stream<Validator<HeaderValidatableT, FailureT>> getHeaderValidatorsStream() {
        var withSimpleValidatorsOrFailWithStream = Stream.ofNullable(withSimpleValidatorsOrFailWith)
                .map(s -> s.apply(ValidatorLiftUtil::liftAllSimple)).flatMap(Collection::stream);
        var withSimpleValidatorStream = withSimpleValidators.stream().map(sv -> sv.apply(ValidatorLiftUtil::liftSimple));
        return Stream.of(withSimpleValidatorsOrFailWithStream, withSimpleValidatorStream, withValidators.stream()).flatMap(identity());
    }

    public String getFieldNameForBatch(Class<HeaderValidatableT> validatableClazz) {
        return PropertyUtils.getPropertyName(validatableClazz, withBatchMapper);
    }
}
