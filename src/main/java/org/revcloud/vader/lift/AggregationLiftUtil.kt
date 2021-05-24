@file:JvmName("AggregationLiftUtil")

package org.revcloud.vader.lift

import org.revcloud.vader.types.validators.Validator

/**
 * Lifts a simple member validation to container type.
 * @param memberValidation Validation on the Member
 * @param toMemberMapper Mapper function to extract member from container
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return Container type validation
 */
fun <ContainerT, MemberT, FailureT> liftToContainerValidatorType(
    memberValidation: Validator<MemberT?, FailureT?>,
    toMemberMapper: (ContainerT?) -> MemberT?,
): Validator<ContainerT?, FailureT?> =
    Validator { memberValidation.apply(toMemberMapper(it)) }

/**
 * Lifts a list of simple member validations to container type.
 * @param memberValidations  List of member validations
 * @param toMemberMapper     Mapper function to extract member from container
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return List of container type validations
 */
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
    memberValidations: Collection<Validator<MemberT?, FailureT?>>,
    toMemberMapper: (ContainerT?) -> MemberT?,
): List<Validator<ContainerT?, FailureT?>> =
    memberValidations.map { liftToContainerValidatorType(it, toMemberMapper) }
