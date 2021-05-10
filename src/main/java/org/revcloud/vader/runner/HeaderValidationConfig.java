package org.revcloud.vader.runner;

import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.revcloud.vader.lift.ValidatorLiftUtil;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<HeaderValidatableT, FailureT> {
    @Singular
    Collection<TypedPropertyGetter<HeaderValidatableT, Collection<?>>> withBatchMappers;
    @Builder.Default
    Tuple2<Integer, FailureT> shouldHaveMinBatchSize = Tuple.of(Integer.MIN_VALUE, null);
    @Builder.Default
    Tuple2<Integer, FailureT> shouldHaveMaxBatchSize = Tuple.of(Integer.MAX_VALUE, null);
    @Singular
    Collection<Validator<HeaderValidatableT, FailureT>> withHeaderValidators;
    Tuple2<Collection<SimpleValidator<HeaderValidatableT, FailureT>>, FailureT> withSimpleHeaderValidatorsOrFailWith;
    @Singular("withSimpleHeaderValidator")
    Collection<Tuple2<SimpleValidator<HeaderValidatableT, FailureT>, FailureT>> withSimpleHeaderValidators;

    Stream<Validator<HeaderValidatableT, FailureT>> getHeaderValidatorsStream() {
        var withSimpleValidatorsOrFailWithStream = Stream.ofNullable(withSimpleHeaderValidatorsOrFailWith)
                .map(s -> s.apply(ValidatorLiftUtil::liftAllSimple)).flatMap(Collection::stream);
        var withSimpleValidatorStream = withSimpleHeaderValidators.stream().map(sv -> sv.apply(ValidatorLiftUtil::liftSimple));
        return Stream.of(withSimpleValidatorsOrFailWithStream, withSimpleValidatorStream, withHeaderValidators.stream()).flatMap(identity());
    }

    public Set<String> getFieldNamesForBatch(Class<HeaderValidatableT> validatableClazz) {
        return withBatchMappers.stream().map(mapper -> PropertyUtils.getPropertyName(validatableClazz, mapper)).collect(Collectors.toSet());
    }
}
