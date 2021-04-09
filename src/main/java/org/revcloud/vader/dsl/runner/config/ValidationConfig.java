package org.revcloud.vader.dsl.runner.config;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Getter;

@Getter
public final class ValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    
    private ValidationConfig() {
    }

    @SuppressWarnings("unused")
    public static <ValidatableT, FailureT> ValidationConfig<ValidatableT, FailureT> toValidate(Class<ValidatableT> validatableTClass, Class<FailureT> failureTClass) {
        return new ValidationConfig<>();
    }
    
    @SafeVarargs
    public final ValidationConfig<ValidatableT, FailureT> shouldHaveRequiredFields(Tuple2<Function1<ValidatableT, ?>, FailureT>... mandatoryFieldMappers) {
        this.mandatoryFieldMappers = this.mandatoryFieldMappers.appendAll(List.of(mandatoryFieldMappers));
        return this;
    }

    @SafeVarargs
    public final ValidationConfig<ValidatableT, FailureT> shouldHaveValidSFIds(Tuple2<Function1<ValidatableT, ID>, FailureT>... sfIdFieldMappers) {
        this.mandatorySfIdFieldMappers = this.mandatorySfIdFieldMappers.appendAll(List.of(sfIdFieldMappers));
        return this;
    }
}
