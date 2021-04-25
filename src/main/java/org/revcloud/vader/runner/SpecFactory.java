package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Function2;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class SpecFactory<ValidatableT, FailureT> {
    SpecFactory() {
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_1nv.Spec_1nvBuilder<ValidatableT, FailureT, ?, ?> _1n() {
        return Spec_1nv.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_1nf.Spec_1nfBuilder<ValidatableT, FailureT, ?, ?> _1nf() {
        return Spec_1nf.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_11_1nf.Spec_11_1nfBuilder<ValidatableT, FailureT, ?, ?> _11_1nf() {
        return Spec_11_1nf.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_11_1n.Spec_11_1nBuilder<ValidatableT, FailureT, ?, ?> _11_1n() {
        return Spec_11_1n.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <GivenT, ThenT> Spec_1n_1n.Spec_1n_1nBuilder<ValidatableT, FailureT, GivenT, ThenT, ?, ?> _1n_1n() {
        return Spec_1n_1n.check();
    }

    @Getter
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    public abstract static class BaseSpec<ValidatableT, FailureT> {
        protected String nameForTest;
        protected FailureT orFailWith;

        protected abstract Predicate<ValidatableT> toPredicate();

        protected FailureT getFailure(ValidatableT ignore) {
            return orFailWith;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_1nv<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> given;
        @NonNull
        Matcher<?> shouldBe;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val actualFieldValue = getGiven().apply(validatable);
                return getShouldBe().matches(actualFieldValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_1nf<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        @Singular("shouldMatchField")
        protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyField;
        @NonNull
        Function1<ValidatableT, ?> given;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val actualFieldValue = getGiven().apply(validatable);
                return getShouldMatchAnyField().stream()
                        .anyMatch(expectedFieldMapper -> expectedFieldMapper.apply(validatable) == actualFieldValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_1nf<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @Singular("shouldMatchField")
        protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyField;
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, ?> then;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (whenValue != getIs()) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);
                return getShouldMatchAnyField().stream()
                        .anyMatch(expectedFieldMapper -> expectedFieldMapper.apply(validatable) == thenValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_1n<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, ?> then;
        Matcher<?> shouldBe;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (whenValue != getIs()) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);
                return shouldBe != null && shouldBe.matches(thenValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_1n_1n<ValidatableT, FailureT, GivenT, ThenT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, GivenT> given;
        @NonNull
        Function1<ValidatableT, ThenT> then;
        @NonNull
        Map<? extends GivenT, ? extends Set<? extends ThenT>> shouldMatch;
        Function2<GivenT, ThenT, FailureT> orFailWithFn;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val givenValue = getGiven().apply(validatable);
                val thenValue = getThen().apply(validatable);
                val validThenValues = getShouldMatch().get(givenValue);
                return validThenValues != null && validThenValues.contains(thenValue);
            };
        }

        @Override
        protected FailureT getFailure(ValidatableT validatable) {
            return orFailWithFn.apply(given.apply(validatable), then.apply(validatable));
        }
    }
}

