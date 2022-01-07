package org.revcloud.vader.specs.specs;

import io.vavr.Function1;
import io.vavr.Function2;
import java.util.Collection;
import java.util.Map;
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
public class Spec4<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {

  @Singular("whenFieldMatches")
  @NonNull
  Map<@Nullable Function1<ValidatableT, ?>, @Nullable Matcher<?>> whenFieldsMatch;

  @Singular("thenFieldShouldMatch")
  @NonNull
  Map<@Nullable Function1<ValidatableT, ?>, @Nullable Matcher<?>> thenFieldsShouldMatch;

  @Nullable Function2<Collection<?>, Collection<?>, ? extends FailureT> orFailWithFn;

  @Override
  public Predicate<@Nullable ValidatableT> toPredicate() {
    return SpecEx.toPredicateEx(this);
  }
}
