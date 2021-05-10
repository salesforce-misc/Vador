/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.vader.lift;

import io.vavr.Function1;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DSL to lift member validations to container type.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class AggregationLiftUtil {

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
    public static <ContainerT, MemberT, FailureT> List<Validator<ContainerT, FailureT>> liftAllToContainerValidatorType(
            Collection<Validator<MemberT, FailureT>> childValidations,
            Function1<ContainerT, MemberT> toChildMapper, FailureT invalidParent, FailureT invalidChild) {
        return childValidations.stream().map(childValidation ->
                liftToContainerValidatorType(childValidation, toChildMapper, invalidParent, invalidChild)).collect(Collectors.toList());
    }

    /**
     * Lifts a list of member validations to container type.
     * IMP: This doesn't do a null check on member, so the member validation is supposed to take that responsibility.
     *
     * @param childValidations List of member validations
     * @param toChildMapper    Mapper function to extract member from container
     * @param invalidParent    Failure to return if container is null
     * @param <ContainerT>
     * @param <MemberT>
     * @param <FailureT>
     * @return List of container type validations
     */
    public static <ContainerT, MemberT, FailureT> List<Validator<ContainerT, FailureT>> liftAllToContainerValidatorType(
            Collection<Validator<MemberT, FailureT>> childValidations,
            Function1<ContainerT, MemberT> toChildMapper, FailureT invalidParent) {
        return childValidations.stream().map(childValidation ->
                liftToContainerValidatorType(childValidation, toChildMapper, invalidParent)).collect(Collectors.toList());
    }

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
     */
    public static <ContainerT, MemberT, FailureT> Validator<ContainerT, FailureT> liftToContainerValidatorType(
            Validator<MemberT, FailureT> memberValidation,
            Function1<ContainerT, MemberT> toMemberMapper,
            FailureT invalidParent, FailureT invalidChild) {
        return validatedContainer -> {
            val member = extractMember(toMemberMapper, invalidParent, validatedContainer);
            return member.isLeft() ? member
                    : member.filter(Objects::nonNull)
                    .fold(() -> Either.left(invalidChild), memberValidation.unchecked());
        };// This whole function is inside a CheckedFunction, so no problem with `uncChecked()` above
    }

    private static <ContainerT, MemberT, FailureT> Either<FailureT, MemberT> extractMember(
            Function1<ContainerT, MemberT> toMemberMapper,
            FailureT invalidParent,
            Either<FailureT, ContainerT> validatedContainer) {
        return validatedContainer
                .flatMap(container -> container == null ? Either.left(invalidParent) : Either.right(container))
                .map(toMemberMapper);
    }

    /**
     * Lifts a member validation to container type.
     * IMP: This doesn't do a null check on member. If the Member is null, the validation throws a NPE while executing.
     * So the member validation is supposed to take that responsibility to check for null.
     * This is specific to validations which want to check other params, based on member being null.
     *
     * @param childValidation
     * @param toChildMapper   Mapper function to extract member from container
     * @param invalidParent   Failure to return if container is null
     * @param <ContainerT>
     * @param <MemberT>
     * @param <FailureT>
     * @return container type validation
     */
    public static <ContainerT, MemberT, FailureT> Validator<ContainerT, FailureT> liftToContainerValidatorType(
            Validator<MemberT, FailureT> childValidation,
            Function1<ContainerT, MemberT> toChildMapper,
            FailureT invalidParent) {
        return validatedContainer -> {
            val member = extractMember(toChildMapper, invalidParent, validatedContainer);
            return member.isLeft() ? member : childValidation.apply(member);
        };
    }
}
