@file:JvmName("IntMatchers")

package org.revcloud.vader.matchers

import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo

fun inRangeInclusive(start: Int, end: Int): Matcher<Int> =
  allOf(greaterThanOrEqualTo(start), lessThanOrEqualTo(end))
