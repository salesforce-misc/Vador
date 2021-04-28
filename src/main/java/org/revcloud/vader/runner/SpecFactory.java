package org.revcloud.vader.runner;

import io.vavr.Function1;
import kotlin.jvm.functions.Function2;
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

import static org.hamcrest.Matchers.is;

public final class SpecFactory<ValidatableT, FailureT> {
    SpecFactory() {
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_1n.Spec_1nBuilder<ValidatableT, FailureT, ?, ?> _1n() {
        return Spec_1n.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_1n_1.Spec_1n_1Builder<ValidatableT, FailureT, ?, ?> _1n_1() {
        return Spec_1n_1.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_11_11_n.Spec_11_11_nBuilder<ValidatableT, FailureT, ?, ?> _11_11_n() {
        return Spec_11_11_n.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_11_11.Spec_11_11Builder<ValidatableT, FailureT, ?, ?> _11_11() {
        return Spec_11_11.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final Spec_11_1n.Spec_11_1nBuilder<ValidatableT, FailureT, ?, ?> _11_1n() {
        return Spec_11_1n.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <Then1T, Then2T> Spec_11_1n_1n.Spec_11_1n_1nBuilder<ValidatableT, FailureT, Then1T, Then2T, ?, ?> _11_1n_1n() {
        return Spec_11_1n_1n.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <Then1T, Then2T> Spec_11_1_1.Spec_11_1_1Builder<ValidatableT, FailureT, Then1T, Then2T, ?, ?> _11_1_1() {
        return Spec_11_1_1.check();
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

        @SuppressWarnings("unused")
        protected FailureT getFailure(ValidatableT ignore) {
            return orFailWith;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_11<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, ?> then;
        Object shouldBe;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (!is(whenValue).matches(getIs())) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);
                return is(thenValue).matches(getShouldBe());
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_1_1<ValidatableT, FailureT, Then1T, Then2T> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, Then1T> thenField1;
        @NonNull
        Function1<ValidatableT, Then2T> thenField2;
        @NonNull
        Function2<Then1T, Then2T, Boolean> shouldRelateWithFn;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (!is(whenValue).matches(getIs())) {
                    return true;
                }
                val thenValue1 = getThenField1().apply(validatable);
                val thenValue2 = getThenField2().apply(validatable);
                return getShouldRelateWithFn().invoke(thenValue1, thenValue2);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_1n_1n<ValidatableT, FailureT, Then1T, Then2T> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, Then1T> thenField1;
        @NonNull
        Function1<ValidatableT, Then2T> thenField2;

        Function2<Then1T, Then2T, Boolean> shouldRelateWithFn;
        Map<? extends Then1T, ? extends Set<? extends Then2T>> shouldRelateWith;
        @Singular("orField1ShouldBe")
        Collection<Matcher<?>> orField1ShouldBeAnyOf;
        @Singular("orField2ShouldBe")
        Collection<Matcher<?>> orField2ShouldBeAnyOf;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            if ((getShouldRelateWithFn() == null) == (getShouldRelateWith() == null)) {
                throw new IllegalArgumentException("Either shouldRelateWithFn or shouldRelatedWith should be provided");
            }
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (!is(whenValue).matches(getIs())) {
                    return true;
                }
                val thenValue1 = getThenField1().apply(validatable);
                val thenValue2 = getThenField2().apply(validatable);
                var relationResult = false;
                if (getShouldRelateWithFn() != null) {
                    relationResult = getShouldRelateWithFn().invoke(thenValue1, thenValue2);
                } else if (getShouldRelateWith() != null) {
                    val validThen2Values = getShouldRelateWith().get(thenValue1);
                    relationResult = validThen2Values != null && validThen2Values.contains(thenValue2);
                }
                return relationResult ||
                        getOrField1ShouldBeAnyOf().stream().anyMatch(matcher -> matcher.matches(thenValue1)) ||
                        getOrField2ShouldBeAnyOf().stream().anyMatch(matcher -> matcher.matches(thenValue2));
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_1n<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
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
    static class Spec_1n_1<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        @Singular("shouldMatchField")
        protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyOfFields;
        @NonNull
        Function1<ValidatableT, ?> given;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val actualFieldValue = getGiven().apply(validatable);
                return getShouldMatchAnyOfFields().stream()
                        .anyMatch(expectedFieldMapper -> expectedFieldMapper.apply(validatable) == actualFieldValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    @SuppressWarnings("java:S101")
    static class Spec_11_11_n<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, ?> when;
        Object is;
        @NonNull
        Function1<ValidatableT, ?> then;
        // TODO 27/04/21 gopala.akshintala: add shouldMatchField and orShouldMatchField 
        @Singular("shouldMatchField")
        protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyOfFields;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if (!is(whenValue).matches(getIs())) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);
                return getShouldMatchAnyOfFields().stream()
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
                if (!is(whenValue).matches(getIs())) {
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
        Map<? extends GivenT, ? extends Set<? extends ThenT>> shouldRelateWith;
        io.vavr.Function2<GivenT, ThenT, FailureT> orFailWithFn;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val givenValue = getGiven().apply(validatable);
                val thenValue = getThen().apply(validatable);
                val validThenValues = getShouldRelateWith().get(givenValue);
                return validThenValues != null && validThenValues.contains(thenValue);
            };
        }

        @Override
        protected FailureT getFailure(ValidatableT validatable) {
            return orFailWithFn.apply(given.apply(validatable), then.apply(validatable));
        }
    }
}

