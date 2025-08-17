/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
@file:JvmName("Destructures")

package com.salesforce.vador.specs

import io.vavr.Tuple2
import io.vavr.Tuple3

// ! TODO gopala.akshintala 13/04/22: Move to a common module
operator fun <T1> Tuple2<T1, *>.component1(): T1 = this._1

operator fun <T2> Tuple2<*, T2>.component2(): T2 = this._2

operator fun <T1> Tuple3<T1, *, *>.component1(): T1 = this._1

operator fun <T2> Tuple3<*, T2, *>.component2(): T2 = this._2

operator fun <T2> Tuple3<*, *, T2>.component3(): T2 = this._3
