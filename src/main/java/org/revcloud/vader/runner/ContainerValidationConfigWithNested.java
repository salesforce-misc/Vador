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
public class ContainerValidationConfigWithNested<
        ContainerValidatableT, NestedContainerValidatableT, FailureT>
    extends BaseContainerValidationConfig<ContainerValidatableT, FailureT> {

  @Singular
  Collection<
          TypedPropertyGetter<
              ContainerValidatableT, @Nullable Collection<NestedContainerValidatableT>>>
      withBatchMappers;

  ContainerValidationConfig<NestedContainerValidatableT, FailureT>
      withNestedContainerValidationConfig;

  public Set<String> getFieldNamesForNestedBatch(
      Class<NestedContainerValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForNestedBatchEx(this, validatableClazz);
  }

  public Set<String> getFieldNamesForBatch(Class<ContainerValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
