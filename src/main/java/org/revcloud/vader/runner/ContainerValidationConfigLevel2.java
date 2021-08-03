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
import org.jetbrains.annotations.Nullable;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class ContainerValidationConfigLevel2<
        ContainerLevel1ValidatableT, ContainerLevel2ValidatableT, FailureT>
    extends BaseContainerValidationConfig<ContainerLevel1ValidatableT, FailureT> {

  @Singular
  Collection<
          TypedPropertyGetter<
              ContainerLevel1ValidatableT, @Nullable Collection<ContainerLevel2ValidatableT>>>
      withBatchMappers;

  ContainerValidationConfig<ContainerLevel2ValidatableT, FailureT>
      withContainerLevel2ValidationConfig;

  public Set<String> getFieldNamesForBatchLevel2(
      Class<ContainerLevel2ValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForBatchLevel2Ex(this, validatableClazz);
  }

  public Set<String> getFieldNamesForBatchLevel1(
      Class<ContainerLevel1ValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
