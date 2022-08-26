/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("DateMatchers")

package org.revcloud.vador.matchers

import io.vavr.Function2
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

val isOnOrBeforeIfBothArePresent = Function2 { date1: Any?, date2: Any? ->
  when {
    date1 == null || date2 == null -> true
    date1 !is Date || date2 !is Date -> false
    else -> date1 == date2 || date1.before(date2)
  }
}

val isBeforeIfBothArePresent = Function2 { date1: Any?, date2: Any? ->
  when {
    date1 == null && date2 == null -> true
    date1 !is Date || date2 !is Date -> false
    else -> date1.before(date2)
  }
}

@Suppress("DEPRECATION")
val isEqualToDayOfDate = Function2 { day: Any?, date: Any? ->
  when {
    day == null && date == null -> false
    day !is Int || date !is Date -> false
    else -> day == date.date
  }
}

@get:JvmName("ISO8601DateFormat")
val ISO8601DateFormat: Matcher<Any?> = object : TypeSafeMatcher<Any?>() {

  override fun describeTo(description: Description?) {
    description?.appendText("ISO 8601 format")
  }

  override fun matchesSafely(date: Any?): Boolean =
    date is String && isValidIS0LocalDateFormat(date)
}

private fun isValidIS0LocalDateFormat(dateAsString: String): Boolean =
  runCatching { parseStringToISOLocalDate(dateAsString) }.isSuccess

private fun parseStringToISOLocalDate(date: String): LocalDate =
  LocalDate.parse(date.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
