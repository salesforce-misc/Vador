package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
    @Builder.Default
    protected List<Tuple2<Function1<ValidatableT, ?>, FailureT>> shouldHaveFields = List.empty();
    @Builder.Default
    protected List<Tuple2<Function1<ValidatableT, ID>, FailureT>> shouldHaveValidSFIds = List.empty();
    @Builder.Default
    protected List<Tuple2<Function1<ValidatableT, ID>, FailureT>> mayHaveValidSFIds = List.empty();
    @Builder.Default
    protected List<Condition<ValidatableT, FailureT>> withConditions = List.empty();
}
