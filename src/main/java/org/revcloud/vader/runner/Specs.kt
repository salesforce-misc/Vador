package org.revcloud.vader.runner

import org.revcloud.vader.runner.SpecFactory.BaseSpec.BaseSpecBuilder

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
