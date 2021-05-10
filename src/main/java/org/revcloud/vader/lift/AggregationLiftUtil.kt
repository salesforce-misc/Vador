package org.revcloud.vader.lift

import io.vavr.Function1
import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.Validator

/**
 * Lifts a member validation to container type.
 *
 * @param memberValidation
 * @param toMemberMapper   Mapper function to extract member from container
 * @param invalidParent    Failure to return if container is null
 * @param invalidChild     Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return container type validation
</FailureT></MemberT></ContainerT> */
fun <ContainerT, MemberT, FailureT> liftToContainerValidatorType(
    memberValidation: Validator<MemberT, FailureT>,
    toMemberMapper: Function1<ContainerT, MemberT>,
    invalidParent: FailureT,
    invalidChild: FailureT
): Validator<ContainerT, FailureT> =
    Validator { validatedContainer ->
        val member = extractMember(toMemberMapper, invalidParent, validatedContainer)
        when {
            member == null -> left<FailureT, ContainerT>(invalidChild)
            member.isLeft -> member
            else -> memberValidation.unchecked().apply(member)
        }
    } // This whole function is inside a CheckedFunction, so no problem with `uncChecked()` above

private fun <ContainerT, MemberT, FailureT> extractMember(
    toMemberMapper: Function1<ContainerT, MemberT>,
    invalidParent: FailureT,
    validatedContainer: Either<FailureT, ContainerT>
): Either<FailureT, MemberT>? =
    validatedContainer.flatMap { container ->
        if (container == null) left(invalidParent) else right(container)
    }.map(toMemberMapper)

/**
 * Lifts a list of member validations to container type.
 *
 * @param childValidations List of member validations
 * @param toChildMapper    Mapper function to extract member from container
 * @param invalidParent    Failure to return if container is null
 * @param invalidChild     Failure to return if member is null
 * @param <ContainerT>
 * @param <MemberT>
 * @param <FailureT>
 * @return List of container type validations
*/
fun <ContainerT, MemberT, FailureT> liftAllToContainerValidatorType(
    childValidations: Collection<Validator<MemberT, FailureT>>,
    toChildMapper: Function1<ContainerT, MemberT>, invalidParent: FailureT, invalidChild: FailureT
): List<Validator<ContainerT, FailureT>> {
    return childValidations.map { childValidation ->
        liftToContainerValidatorType(
            childValidation,
            toChildMapper,
            invalidParent,
            invalidChild
        )
    }
}
