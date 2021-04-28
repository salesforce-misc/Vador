package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
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
    public final <GivenT> Spec1.Spec1Builder<ValidatableT, FailureT, GivenT, ?, ?> _1() {
        return Spec1.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <WhenT, ThenT> Spec2.Spec2Builder<ValidatableT, FailureT, WhenT, ThenT, ?, ?> _2() {
        return Spec2.check();
    }

    @SuppressWarnings({"java:S100", "java:S1452"})
    public final <WhenT, Then1T, Then2T> Spec3.Spec3Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T, ?, ?> _3() {
        return Spec3.check();
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
    static class Spec1<ValidatableT, FailureT, GivenT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, GivenT> given;
        GivenT shouldBe;
        @Singular("shouldMatchField")
        protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyOfFields;
        @Singular("shouldMatch")
        Collection<? extends Matcher<GivenT>> shouldMatchAnyOf;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val givenValue = getGiven().apply(validatable);
                return is(shouldBe).matches(givenValue) ||
                        getShouldMatchAnyOf().stream().anyMatch(m -> m.matches(givenValue)) ||
                        getShouldMatchAnyOfFields().stream()
                                .anyMatch(expectedFieldMapper -> expectedFieldMapper.apply(validatable) == givenValue);
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    static class Spec2<ValidatableT, FailureT, WhenT, ThenT> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, WhenT> when;
        WhenT is;
        @Singular("matches")
        Collection<Matcher<WhenT>> matchesAnyOf;
        // TODO 28/04/21 gopala.akshintala: add match fields 
        @NonNull
        Function1<ValidatableT, ThenT> then;
        ThenT shouldBe;
        // TODO 28/04/21 gopala.akshintala: Think about having `or` prefix 
        @Singular("shouldMatch")
        Collection<? extends Matcher<ThenT>> shouldMatchAnyOf;
        Map<? extends WhenT, ? extends Set<? extends ThenT>> shouldRelateWith;
        Function2<WhenT, ThenT, Boolean> shouldRelateWithFn;
        Function2<WhenT, ThenT, FailureT> orFailWithFn;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if ((shouldRelateWith == null && shouldRelateWithFn == null) &&
                        (!is(getIs()).matches(whenValue) && getMatchesAnyOf().stream().noneMatch(m -> m.matches(whenValue)))) {
                    return true;
                }
                val thenValue = getThen().apply(validatable);

                val result = is(shouldBe).matches(thenValue) ||
                        getShouldMatchAnyOf().stream().anyMatch(m -> m.matches(thenValue));
                if (!result) {
                    return (getShouldRelateWith() != null && getShouldRelateWith().get(whenValue) != null && getShouldRelateWith().get(whenValue).contains(thenValue)) ||
                            (getShouldRelateWithFn() != null && getShouldRelateWithFn().apply(whenValue, thenValue));
                }
                return true;
            };
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
    static class Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T> extends BaseSpec<ValidatableT, FailureT> {
        @NonNull
        Function1<ValidatableT, WhenT> when;
        WhenT is;
        @Singular("matches")
        Collection<Matcher<WhenT>> matchesAnyOf;
        @NonNull
        Function1<ValidatableT, Then1T> thenField1;
        @NonNull
        Function1<ValidatableT, Then2T> thenField2;
        Then1T field1ShouldBe;
        @Singular("field1ShouldMatch")
        Collection<Matcher<?>> field1ShouldMatchAnyOf;
        Then2T field2ShouldBe;
        @Singular("field2ShouldMatch")
        Collection<Matcher<?>> field2ShouldMatchAnyOf;
        Function2<Then1T, Then2T, Boolean> shouldRelateWithFn;
        Map<? extends Then1T, ? extends Set<? extends Then2T>> shouldRelateWith;
        Function3<WhenT, Then1T, Then2T, FailureT> orFailWithFn;

        @Override
        public Predicate<ValidatableT> toPredicate() {
            return validatable -> {
                val whenValue = getWhen().apply(validatable);
                if ((shouldRelateWith == null && shouldRelateWithFn == null) &&
                        (!is(getIs()).matches(whenValue) && getMatchesAnyOf().stream().noneMatch(m -> m.matches(whenValue)))) {
                    return true;
                }
                val thenValue1 = getThenField1().apply(validatable);
                val thenValue2 = getThenField2().apply(validatable);
                var relationResult = false;
                if (getShouldRelateWithFn() != null) {
                    relationResult = getShouldRelateWithFn().apply(thenValue1, thenValue2);
                } else if (getShouldRelateWith() != null) {
                    val validThen2Values = getShouldRelateWith().get(thenValue1);
                    relationResult = validThen2Values != null && validThen2Values.contains(thenValue2);
                }
                return relationResult ||
                        getField1ShouldMatchAnyOf().stream().anyMatch(matcher -> matcher.matches(thenValue1)) ||
                        getField2ShouldMatchAnyOf().stream().anyMatch(matcher -> matcher.matches(thenValue2));
            };
        }

        @Override
        protected FailureT getFailure(ValidatableT validatable) {
            return orFailWithFn.apply(getWhen().apply(validatable), getThenField1().apply(validatable), getThenField2().apply(validatable));
        }
    }

}

