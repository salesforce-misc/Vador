package org.revcloud.vader.specs.specs;

import io.vavr.Function1;
import io.vavr.Function2;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;

@Value
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec2<ValidatableT, FailureT, WhenT, ThenT> extends BaseSpec<ValidatableT, FailureT> {

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
  public FailureT getFailure(@Nullable ValidatableT validatable) {
    if ((orFailWith == null) == (orFailWithFn == null)) {
      throw new IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest));
    }
    if (orFailWith != null) {
      return orFailWith;
    }
    return orFailWithFn.apply(getWhen().apply(validatable), getThen().apply(validatable));
  }
}
