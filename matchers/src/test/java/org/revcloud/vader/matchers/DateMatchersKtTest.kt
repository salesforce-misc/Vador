/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.matchers

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import java.util.Calendar
import java.util.GregorianCalendar

class DateMatchersKtTest : StringSpec({
  "is Date's day matching" {
    forAll(
      row(1, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, true),
      row(2, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, false),
      row(null, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, false),
      row(null, null, false)
    ) { day, date, result ->
      isEqualToDayOfDate.apply(day, date) shouldBe result
    }
  }

  "ISO8601Format" {
    ISO8601DateFormat.matches("2021-07-30") shouldBe true
    ISO8601DateFormat.matches("2019-02-05T21:22:41.000Z") shouldBe false
  }

  "Is On Or Before If Both Are Present" {
    forAll(
      row(null, null, true),
      row(null, "", true),
      row("2021-12-28", "2021-12-28", false),
      row(GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, "", false),
      row(GregorianCalendar(2021, Calendar.FEBRUARY, 2).time, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, false),
      row(GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, true),
      row(GregorianCalendar(2021, Calendar.JANUARY, 31).time, GregorianCalendar(2021, Calendar.FEBRUARY, 1).time, true)
    ) { date1, date2, result ->
      isOnOrBeforeIfBothArePresent.apply(date1, date2) shouldBe result
    }
  }
})
