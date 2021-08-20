@file:JvmName("Extensions")

package org.revcloud.vader.runner

import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.control.Either
import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.lift.liftToEtr
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

internal operator fun <T1> Tuple2<T1, *>?.component1(): T1? = this?._1
internal operator fun <T2> Tuple2<*, T2>?.component2(): T2? = this?._2

internal operator fun <T1> Tuple3<T1, *, *>?.component1(): T1? = this?._1
internal operator fun <T2> Tuple3<*, T2, *>?.component2(): T2? = this?._2
internal operator fun <T2> Tuple3<*, *, T2>?.component3(): T2? = this?._3

internal fun <ValidatableT, FailureT> fromValidators1(validators: Tuple2<out Collection<Validator<in ValidatableT?, FailureT?>>?, out FailureT?>?): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  validators?.let { (svs, none) -> svs?.let { liftAllToEtr(it, none) } } ?: emptyList()

internal fun <ValidatableT, FailureT> fromValidators2(validators: Map<out Validator<in ValidatableT?, FailureT?>, FailureT?>): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  validators.mapNotNull { (sv, none) -> liftToEtr(sv, none) }

internal fun <FailureT> Either<FailureT?, *>?.toFailureOptional(): Optional<FailureT> {
  val swapped = this?.swap() ?: return Optional.empty()
  return if (swapped.isEmpty) Optional.empty() else Optional.ofNullable(swapped.get())
}

internal fun <FailureT, PairT> Either<Tuple2<PairT?, FailureT?>, *>?.toFailureWithPairOptional(): Optional<Tuple2<PairT?, FailureT?>> {
  val swapped = this?.swap() ?: return Optional.empty()
  return if (swapped.isEmpty) Optional.empty() else Optional.ofNullable(swapped.get())
}
