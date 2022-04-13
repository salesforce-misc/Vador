@file:JvmName("AggregationLiftEtrUtil")

package org.revcloud.vader.lift

import org.revcloud.vader.types.ValidatorEtr

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
  memberValidator: ValidatorEtr<MemberT?, FailureT?>,
  toMemberMapper: (ContainerT?) -> MemberT?,
): ValidatorEtr<ContainerT?, FailureT?> =
  ValidatorEtr { container ->
    val member = container?.map(toMemberMapper)
    memberValidator.unchecked().apply(member)
  } // This whole function is inside a CheckedFunction, so no problem with `uncChecked()` above

/**
 * Lifts a list of member validations to container type.
 *
 * @param memberValidatorEtrs List of member validations
 * @param toChildMapper    Mapper function to extract member from container
 * @param invalidParent    Failure to return if container is null
 * @param invalidChild     Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return List of container type validations
 */
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
  memberValidatorEtrs: Collection<ValidatorEtr<MemberT?, FailureT?>>,
  toMemberMapper: (ContainerT?) -> MemberT?,
): List<ValidatorEtr<ContainerT?, FailureT?>> =
  memberValidatorEtrs.map { liftToContainerValidatorType(it, toMemberMapper) }
