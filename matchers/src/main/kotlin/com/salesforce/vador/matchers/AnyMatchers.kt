/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("AnyMatchers")

package com.salesforce.vador.matchers

import org.hamcrest.Matcher
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.AnyOf
import org.hamcrest.core.IsNull

fun <T> anyOf(vararg ts: T): AnyOf<T> = anyOf(ts.map { `is`(it) })

@SafeVarargs
fun <T> anyOf(vararg matchers: Matcher<T>): AnyOf<T> = anyOf(*matchers)

@SafeVarargs
fun <T> anyOfOrNull(vararg matchers: Matcher<T>): AnyOf<T> = anyOf(matchers.toList() + IsNull())
