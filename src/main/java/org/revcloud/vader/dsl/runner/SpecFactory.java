package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.Function2;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.revcloud.vader.dsl.runner.Utils.matchFields;

public final class SpecFactory<ValidatableT, FailureT> {
    SpecFactory() {
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec1.Spec1Builder<ValidatableT, FailureT, ?, ?> _1n() {
        return Spec1.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec2.Spec2Builder<ValidatableT, FailureT, ?, ?> _11_1n() {
        return Spec2.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <WhenT, ThenT> Spec3.Spec3Builder<ValidatableT, FailureT, WhenT, ThenT, ?, ?> _1n_1n() {
        return Spec3.check();
    }

    // TODO 23/04/21 gopala.akshintala: Check on the inheritance 
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

        protected FailureT getFailure(ValidatableT ignore) {
            return orFailWith;
        }
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
                val whenValue = getWhen().apply(validatable);
                if (whenValue != getIs()) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);
                return shouldBe != null && shouldBe.matches(thenValue) ||
                        matchFields(this, validatable, thenValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    static class Spec3<ValidatableT, FailureT, WhenT, ThenT> extends BaseSpec<ValidatableT, FailureT> {
        Function1<ValidatableT, WhenT> when;
        Function1<ValidatableT, ThenT> then;
        Function2<WhenT, ThenT, FailureT> orFailWithFn;
        Map<? extends WhenT, ? extends Set<? extends ThenT>> shouldMatch;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                val thenValue = getThen().apply(validatable);
                val validThenValues = shouldMatch.get(whenValue);
                return validThenValues.contains(thenValue);
            };
        }

        @Override
        protected FailureT getFailure(ValidatableT validatable) {
            return orFailWithFn.apply(when.apply(validatable), then.apply(validatable));
        }
    }
}

