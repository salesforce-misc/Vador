package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

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
    protected Collection<BaseSpec<ValidatableT, FailureT>> withSpecs;
}
