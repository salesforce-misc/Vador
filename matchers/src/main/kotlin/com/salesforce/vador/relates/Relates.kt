package com.salesforce.vador.relates

fun <WhenT : Any?, ThenT : Any?> onlyOneShouldBeNonNull(w: WhenT, t: ThenT): Boolean =
  listOfNotNull(w, t).size == 1
