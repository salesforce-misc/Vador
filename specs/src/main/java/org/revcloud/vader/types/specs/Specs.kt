package org.revcloud.vader.types.specs

import org.revcloud.vader.SpecFactory

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<SpecFactory.BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
