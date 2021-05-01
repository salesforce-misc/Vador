@file:JvmName("AnyMatchers")

package org.revcloud.vader.matchers

import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.anyOf
import org.hamcrest.core.AnyOf
import org.hamcrest.core.IsNull

fun <T> anyOf(vararg ts: T): AnyOf<T> = anyOf(ts.map { `is`(it) })
fun <T> anyOf(vararg matchers: Matcher<T>): AnyOf<T> = anyOf(*matchers)

@SafeVarargs
fun <T> anyOfOrNull(vararg matchers: Matcher<T>): AnyOf<T> = anyOf(matchers.toList() + IsNull())
