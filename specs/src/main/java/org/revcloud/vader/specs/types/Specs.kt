package org.revcloud.vader.specs.types

import org.revcloud.vader.specs.factory.SpecFactory
import org.revcloud.vader.specs.specs.BaseSpec

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
