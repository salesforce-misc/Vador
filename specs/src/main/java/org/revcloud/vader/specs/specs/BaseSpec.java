package org.revcloud.vader.specs.specs;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Getter
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public abstract class BaseSpec<ValidatableT, FailureT> {

  protected static final String INVALID_FAILURE_CONFIG =
      "For Spec with: %s Either 'orFailWith' or 'orFailWithFn' should be passed, but not both";
  @Nullable protected String nameForTest;
  @Nullable protected FailureT orFailWith;

  public abstract Predicate<@Nullable ValidatableT> toPredicate();

  // TODO 05/06/21 gopala.akshintala: Replace with `when` expression checking instanceOf
  @SuppressWarnings("unused")
  public FailureT getFailure(@Nullable ValidatableT ignore) {
    return orFailWith;
  }
}
