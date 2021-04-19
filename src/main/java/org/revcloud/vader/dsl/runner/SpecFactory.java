package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.function.Predicate;

import static org.revcloud.vader.dsl.runner.Utils.matchFields;

public final class SpecFactory<ValidatableT, FailureT> {
    SpecFactory() {
    }
    
    @SuppressWarnings("java:S116")
    public final Spec1.Spec1Builder<ValidatableT, FailureT, ?, ?> _1 = Spec1.check();
    @SuppressWarnings("java:S116")
    public final Spec2.Spec2Builder<ValidatableT, FailureT, ?, ?> _2 = Spec2.check();

    @Getter
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    public abstract static class BaseSpec<ValidatableT, FailureT> {
        protected String nameForTest;
        protected FailureT orFailWith;
        protected Matcher<?> shouldBe;
        protected Function1<ValidatableT, ?> matchesField;
        @Singular
        protected Collection<Function1<ValidatableT, ?>> orMatchesFields;
    
        protected abstract Predicate<ValidatableT> toPredicate();
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    static class Spec1<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        Function1<ValidatableT, ?> given;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val actualFieldValue = getGiven().apply(validatable);
                final Matcher<?> shouldBe = getShouldBe();
                return shouldBe != null && shouldBe.matches(actualFieldValue) ||
                        matchFields(this, validatable, actualFieldValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    static class Spec2<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        Function1<ValidatableT, ?> when;
        Object is;
        Function1<ValidatableT, ?> then;
    
        // TODO 15/04/21 gopala.akshintala: Check for non-null 
        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val actualFieldValue = getWhen().apply(validatable);
                if (actualFieldValue != getIs()) {
                    return true;
                }
                val actualDependentFieldValue = getThen().apply(validatable);
                final Matcher<?> shouldBe = getShouldBe();
                return shouldBe != null && shouldBe.matches(actualDependentFieldValue) ||
                        matchFields(this, validatable, actualDependentFieldValue);
            };
        }
    }
}

