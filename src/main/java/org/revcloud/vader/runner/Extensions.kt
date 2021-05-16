@file:JvmName("Extensions")

package org.revcloud.vader.runner

import io.vavr.Tuple2
import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.lift.liftSimple
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator

internal operator fun <T1> Tuple2<T1, *>?.component1(): T1? = this?._1
internal operator fun <T2> Tuple2<*, T2>?.component2(): T2? = this?._2

internal fun <ValidatableT, FailureT> fromSimpleValidators1(simpleValidators: Tuple2<out Collection<SimpleValidator<in ValidatableT?, FailureT?>>?, out FailureT?>?): List<Validator<ValidatableT?, FailureT?>> =
    simpleValidators?.let { (svs, none) -> svs?.let { liftAllSimple(it, none) } } ?: emptyList()

internal fun <ValidatableT, FailureT> fromSimpleValidators2(simpleValidators: Collection<Tuple2<out SimpleValidator<in ValidatableT?, FailureT?>, out FailureT?>>): List<Validator<ValidatableT?, FailureT?>> =
    simpleValidators.mapNotNull { (sv, none) -> sv?.let { liftSimple(it, none) } }
