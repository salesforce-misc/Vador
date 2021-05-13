@file:JvmName("AggregationLiftSimpleUtil")

package org.revcloud.vader.lift

import io.vavr.Function1
import org.revcloud.vader.types.validators.SimpleValidator

/**
 * Lifts a simple member validation to container type.
 * @param memberValidation Validation on the Member
 * @param toMemberMapper Mapper function to extract member from container
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return  Container type validation
 */
fun <ContainerT, MemberT, FailureT> liftToContainerValidatorType(
    memberValidation: SimpleValidator<in MemberT?, out FailureT?>,
    toMemberMapper: Function1<in ContainerT?, out MemberT?>,
): SimpleValidator<in ContainerT?, out FailureT?> =
    SimpleValidator { container ->
        val member = toMemberMapper.apply(container)
        memberValidation.apply(member)
    }

/**
 * Lifts a list of simple member validations to container type.
 * @param memberValidations  List of member validations
 * @param toMemberMapper     Mapper function to extract member from container
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return                  List of container type validations
 */
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
    memberValidations: List<SimpleValidator<in MemberT?, out FailureT?>>,
    toMemberMapper: Function1<in ContainerT?, out MemberT?>
): List<SimpleValidator<in ContainerT?, out FailureT?>> =
    memberValidations.map { liftToContainerValidatorType(it, toMemberMapper) }



