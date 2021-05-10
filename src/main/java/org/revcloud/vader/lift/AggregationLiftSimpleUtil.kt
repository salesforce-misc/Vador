@file:JvmName("AggregationLiftSimpleUtil")
package org.revcloud.vader.lift

import io.vavr.Function1
import io.vavr.collection.List
import org.revcloud.vader.types.validators.SimpleValidator

/**
 * Lifts a list of simple member validations to container type.
 * @param memberValidations  List of member validations
 * @param toMemberMapper     Mapper function to extract member from container
 * @param invalidContainer     Failure to return if container is null
 * @param invalidMember      Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return                  List of container type validations
 */
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
    memberValidations: List<SimpleValidator<MemberT, FailureT>>,
    toMemberMapper: Function1<ContainerT, MemberT>, invalidContainer: FailureT, invalidMember: FailureT
): List<SimpleValidator<ContainerT, FailureT>> {
    return memberValidations.map { memberValidation ->
        liftToContainerValidatorType(
            memberValidation,
            toMemberMapper,
            invalidContainer,
            invalidMember
        )
    }
}

/**
 * Lifts a simple member validation to container type.
 * @param memberValidation
 * @param toMemberMapper Mapper function to extract member from container
 * @param invalidContainer Failure to return if container is null
 * @param invalidMember  Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return  container type validation
 */
fun <ContainerT, MemberT, FailureT> liftToContainerValidatorType(
    memberValidation: SimpleValidator<MemberT, FailureT>,
    toMemberMapper: Function1<ContainerT, MemberT>,
    invalidContainer: FailureT, invalidMember: FailureT
): SimpleValidator<ContainerT, FailureT> =
    SimpleValidator { validatedContainer ->
        if (validatedContainer == null) {
            invalidContainer
        } else {
            val member = toMemberMapper.apply(validatedContainer)
            if (member == null) invalidMember else memberValidation.apply(member)
        }
    }


