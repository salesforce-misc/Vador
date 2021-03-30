package org.revcloud.vader.dsl.runner.config;

import com.force.swag.id.ID;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.Getter;

@Getter
public abstract class BaseValidationConfig<ValidatableT, FailureT> {
    protected List<Tuple2<Function1<ValidatableT, ?>, FailureT>> mandatoryFieldMappers = List.empty();
    protected List<Tuple2<Function1<ValidatableT, ID>, FailureT>> sfIdFieldMappers = List.empty();
}
