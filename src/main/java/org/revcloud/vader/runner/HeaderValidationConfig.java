package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import java.util.Collection;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<HeaderValidatableT, FailureT> extends BaseHeaderValidationConfig<HeaderValidatableT, FailureT> {
  @Singular
  Collection<TypedPropertyGetter<HeaderValidatableT, Collection<?>>> withBatchMappers;

  public Set<String> getFieldNamesForBatch(Class<HeaderValidatableT> validatableClazz) {
    return HeaderValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
