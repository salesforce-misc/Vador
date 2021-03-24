package org.qtc.delphinus.dsl.runner;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;

public final class ValidationConfig<ValidatableT, FailureT> {
    List<Tuple2<Function1<ValidatableT, Object>, FailureT>> mandatoryFieldMappers = List.empty();
    List<Tuple2<Function1<ValidatableT, ID>, FailureT>> sfIdFieldMappers = List.empty();
    
    private ValidationConfig() {
    }

    @SuppressWarnings("unchecked")
    public static <ValidatableT, FailureT> ValidationConfig<ValidatableT, FailureT> toValidate(Class<ValidatableT> ignore1, Class<FailureT> ignore2) {
        return new ValidationConfig<>();
    }

    @SafeVarargs
    public final ValidationConfig<ValidatableT, FailureT> shouldHaveRequiredFields(Tuple2<Function1<ValidatableT, Object>, FailureT>... mandatoryFieldMappers) {
        this.mandatoryFieldMappers = this.mandatoryFieldMappers.appendAll(List.of(mandatoryFieldMappers));
        return this;
    }

    @SafeVarargs
    public final ValidationConfig<ValidatableT, FailureT> shouldHaveValidSFIds(Tuple2<Function1<ValidatableT, ID>, FailureT>... sfIdFieldMappers) {
        this.sfIdFieldMappers = this.sfIdFieldMappers.appendAll(List.of(sfIdFieldMappers));
        return this;
    }
}
