@file:JvmName("IntMatchers")

package org.revcloud.vader

import org.hamcrest.Matchers.*

fun inRangeInclusive(start: Int, end: Int) = allOf(greaterThanOrEqualTo(start), lessThanOrEqualTo(end))
