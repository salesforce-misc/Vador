package org.revcloud.vader.specs

import org.revcloud.vader.specs.factory.SpecFactory
import org.revcloud.vader.specs.specs.BaseSpec

fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
