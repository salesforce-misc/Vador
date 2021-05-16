package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;

public final class SpecFactory<ValidatableT, FailureT> {

  private static final String INVALID_FAILURE_CONFIG =
      "For Spec with: %s Either 'orFailWith' or 'orFailWithFn' should be passed, but not both";

  SpecFactory() {}

  @SuppressWarnings({"java:S100", "java:S1452"})
  public final <GivenT> Spec1.Spec1Builder<ValidatableT, FailureT, GivenT, ?, ?> _1() {
    return Spec1.check();
  }

  @SuppressWarnings({"java:S100", "java:S1452"})
  public final <WhenT, ThenT> Spec2.Spec2Builder<ValidatableT, FailureT, WhenT, ThenT, ?, ?> _2() {
    return Spec2.check();
  }

  @SuppressWarnings({"java:S100", "java:S1452"})
  public final <WhenT, Then1T, Then2T>
      Spec3.Spec3Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T, ?, ?> _3() {
    return Spec3.check();
  }

  @Getter
  @FieldDefaults(level = AccessLevel.PACKAGE)
  @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
  public abstract static class BaseSpec<ValidatableT, FailureT> {
    @Nullable protected String nameForTest;
    @Nullable protected FailureT orFailWith;

    protected abstract Predicate<@Nullable ValidatableT> toPredicate();

    @SuppressWarnings("unused")
    protected FailureT getFailure(ValidatableT ignore) {
      return orFailWith;
    }
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  @FieldDefaults(level = AccessLevel.PACKAGE)
  @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
  static class Spec1<ValidatableT, FailureT, GivenT> extends BaseSpec<ValidatableT, FailureT> {
    @Singular("shouldMatchField")
    protected Collection<Function1<ValidatableT, ?>> shouldMatchAnyOfFields;

    @NonNull Function1<ValidatableT, ? extends GivenT> given;

    @Singular("shouldMatch")
    Collection<? extends Matcher<? extends GivenT>> shouldMatchAnyOf;

    @Nullable Function1<GivenT, ? extends FailureT> orFailWithFn;

    @Override
    public Predicate<@NonNull ValidatableT> toPredicate() {
      return SpecEx.toPredicateEx(this);
    }

    @Override
    protected FailureT getFailure(ValidatableT validatable) {
      if ((orFailWith == null) == (orFailWithFn == null)) {
        throw new IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest));
      }
      if (orFailWith != null) {
        return orFailWith;
      }
      return orFailWithFn.apply(getGiven().apply(validatable));
    }
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  @FieldDefaults(level = AccessLevel.PACKAGE)
  @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
  static class Spec2<ValidatableT, FailureT, WhenT, ThenT>
      extends BaseSpec<ValidatableT, FailureT> {
    @NonNull Function1<ValidatableT, ? extends WhenT> when;

    @Singular("matches")
    Collection<? extends Matcher<? extends WhenT>> matchesAnyOf;

    @NonNull Function1<ValidatableT, ? extends ThenT> then;
    // TODO 28/04/21 gopala.akshintala: Think about having `or` prefix
    @Singular("shouldMatch")
    Collection<? extends Matcher<? extends ThenT>> shouldMatchAnyOf;

    @Singular("shouldRelateWithEntry")
    Map<? extends WhenT, ? extends Set<? extends ThenT>> shouldRelateWith;

    @Nullable Function2<WhenT, ThenT, Boolean> shouldRelateWithFn;
    @Nullable Function2<WhenT, ThenT, ? extends FailureT> orFailWithFn;

    @Override
    public Predicate<@Nullable ValidatableT> toPredicate() {
      return SpecEx.toPredicateEx(this);
    }

    @Override
    protected FailureT getFailure(ValidatableT validatable) {
      if ((orFailWith == null) == (orFailWithFn == null)) {
        throw new IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest));
      }
      if (orFailWith != null) {
        return orFailWith;
      }
      return orFailWithFn.apply(getWhen().apply(validatable), getThen().apply(validatable));
    }
  }

  @Value
  @EqualsAndHashCode(callSuper = true)
  @FieldDefaults(level = AccessLevel.PACKAGE)
  @SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
  static class Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>
      extends BaseSpec<ValidatableT, FailureT> {
    @NonNull Function1<ValidatableT, ? extends WhenT> when;

    @Singular("matches")
    Collection<? extends Matcher<? extends WhenT>> matchesAnyOf;

    @NonNull Function1<ValidatableT, ? extends Then1T> thenField1;
    @NonNull Function1<ValidatableT, ? extends Then2T> thenField2;

    @Singular("shouldRelateWithEntry")
    Map<? extends Then1T, ? extends Set<? extends Then2T>> shouldRelateWith;

    @Nullable Function2<Then1T, Then2T, Boolean> shouldRelateWithFn;

    @Singular("orField1ShouldMatch")
    Collection<? extends Matcher<? extends Then1T>> orField1ShouldMatchAnyOf;

    @Singular("orField2ShouldMatch")
    Collection<? extends Matcher<? extends Then2T>> orField2ShouldMatchAnyOf;

    @Nullable Function3<WhenT, Then1T, Then2T, ? extends FailureT> orFailWithFn;

    @Override
    public Predicate<@Nullable ValidatableT> toPredicate() {
      return SpecEx.toPredicateEx(this);
    }

    @Override
    protected FailureT getFailure(ValidatableT validatable) {
      if ((orFailWith == null) && (orFailWithFn == null)) {
        throw new IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest));
      }
      if (orFailWith != null) {
        return orFailWith;
      }
      return orFailWithFn.apply(
          getWhen().apply(validatable),
          getThenField1().apply(validatable),
          getThenField2().apply(validatable));
    }
  }
}
