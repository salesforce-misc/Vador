package org.revcloud.vader.runner

import io.vavr.Tuple2

operator fun <T1> Tuple2<T1, *>?.component1(): T1? = this?._1
operator fun <T2> Tuple2<*, T2>?.component2(): T2? = this?._2
