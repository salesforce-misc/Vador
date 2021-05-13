@file:JvmName("AggregationLiftUtil")

package org.revcloud.vader.lift

import io.vavr.Function1
import org.revcloud.vader.types.validators.Validator

/**
 * Lifts a member validation to container type.
 *
 * @param memberValidator
 * @param toMemberMapper   Mapper function to extract member from container
 * @param nullContainer    Failure to return if container is null
 * @param nullMember     Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return container type validation
</FailureT></MemberT></ContainerT> */
fun <ContainerT, MemberT, FailureT> liftToContainerValidatorType(
    memberValidator: Validator<MemberT?, FailureT?>,
    toMemberMapper: Function1<in ContainerT?, out MemberT?>,
): Validator<ContainerT?, FailureT?> =
    Validator { container ->
        val member = container?.map(toMemberMapper)
        memberValidator.unchecked().apply(member)
    } // This whole function is inside a CheckedFunction, so no problem with `uncChecked()` above

/**
 * Lifts a list of member validations to container type.
 *
 * @param memberValidators List of member validations
 * @param toChildMapper    Mapper function to extract member from container
 * @param invalidParent    Failure to return if container is null
 * @param invalidChild     Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return List of container type validations
 */
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
    memberValidators: Collection<Validator<MemberT?, FailureT?>>,
    toChildMapper: Function1<in ContainerT?, out MemberT?>
): List<Validator<ContainerT?, FailureT?>> =
    memberValidators.map { liftToContainerValidatorType(it, toChildMapper) }
