package org.revcloud.vader.dsl.runner.config;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Getter;

@Getter
public final class BatchValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    
    private Tuple2<FailureT, Function1<ValidatableT, ?>> filterDuplicates = null;
    
    private BatchValidationConfig() {
    }

    @SuppressWarnings("unused")
    public static <ValidatableT, FailureT> BatchValidationConfig<ValidatableT, FailureT> toValidate(Class<ValidatableT> validatableTClass, Class<FailureT> failureTClass) {
        return new BatchValidationConfig<>();
    }
    
    @SafeVarargs
    public final BatchValidationConfig<ValidatableT, FailureT> shouldHaveRequiredFields(Tuple2<Function1<ValidatableT, ?>, FailureT>... mandatoryFieldMappers) {
        this.mandatoryFieldMappers = this.mandatoryFieldMappers.appendAll(List.of(mandatoryFieldMappers));
        return this;
    }

    @SafeVarargs
    public final BatchValidationConfig<ValidatableT, FailureT> shouldHaveValidSFIds(Tuple2<Function1<ValidatableT, ID>, FailureT>... sfIdFieldMappers) {
        this.sfIdFieldMappers = this.sfIdFieldMappers.appendAll(List.of(sfIdFieldMappers));
        return this;
    }
    
    public final BatchValidationConfig<ValidatableT, FailureT> failDuplicatesWith(FailureT failureForDuplicate, Function1<ValidatableT, ?> keyMapperForDuplicates) {
        this.filterDuplicates = Tuple.of(failureForDuplicate, keyMapperForDuplicates);
        return this;
    }
    
}
