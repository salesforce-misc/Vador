package org.revcloud.vader.types.specs

import org.revcloud.vader.SpecFactory

fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, SpecFactory.BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>
