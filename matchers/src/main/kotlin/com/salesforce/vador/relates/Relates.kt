package com.salesforce.vador.relates

fun <WhenT, ThenT> onlyOneShouldBeNonNull(w: WhenT, t: ThenT): Boolean =
  listOfNotNull(w, t).size == 1
