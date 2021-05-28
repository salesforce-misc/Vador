package org.revcloud.vader.runner

import org.revcloud.vader.runner.SpecFactory.BaseSpec.BaseSpecBuilder

fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, BaseSpecBuilder<ValidatableT, FailureT, *, *>>
