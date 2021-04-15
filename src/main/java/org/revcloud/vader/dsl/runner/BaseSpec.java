package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.function.Predicate;

@Getter
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
abstract class BaseSpec<ValidatableT, FailureT> {
    FailureT orFailWith;
    Matcher<?> shouldBe;
    Function1<ValidatableT, ?> matchesField;
    @Singular
    Collection<Function1<ValidatableT, ?>> orMatchesFields;

    protected abstract Predicate<ValidatableT> toPredicate(); 
}

// TODO 14/04/21 gopala.akshintala: Demo reuse of same spec for a change of one of the params 
// TODO 14/04/21 gopala.akshintala: Demo unit testing Spec 
