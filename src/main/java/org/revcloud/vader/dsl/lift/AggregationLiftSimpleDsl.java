/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.vader.dsl.lift;

import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.types.validators.SimpleValidator;

/**
 * DSL to lift simple member validations to container type.
 * 
 *  @author gakshintala
 *  @since 228
 */
@UtilityClass
public class AggregationLiftSimpleDsl {

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
    public static <ContainerT, MemberT, FailureT> List<SimpleValidator<ContainerT, FailureT>> liftAllToContainerValidatorType(
            List<SimpleValidator<MemberT, FailureT>> memberValidations,
            Function1<ContainerT, MemberT> toMemberMapper, FailureT invalidContainer, FailureT invalidMember) {
        return memberValidations.map(memberValidation ->
                liftToContainerValidatorType(memberValidation, toMemberMapper, invalidContainer, invalidMember));
    }

    /**
     * Lifts a list of simple member validations to container type.
     * IMP: This doesn't do a null check on member, so the member validation is supposed to take that responsibility.
     * @param memberValidations  List of member validations
     * @param toMemberMapper     Mapper function to extract member from container
     * @param invalidContainer     Failure to return if container is null
     * @param <ContainerT>
     * @param <MemberT>
     * @param <FailureT>
     * @return                  List of container type validations
     */
    public static <ContainerT, MemberT, FailureT> List<SimpleValidator<ContainerT, FailureT>> liftAllToContainerValidatorType(
            List<SimpleValidator<MemberT, FailureT>> memberValidations,
            Function1<ContainerT, MemberT> toMemberMapper, FailureT invalidContainer) {
        return memberValidations.map(memberValidation ->
                liftToContainerValidatorType(memberValidation, toMemberMapper, invalidContainer));
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
    public static <ContainerT, MemberT, FailureT> SimpleValidator<ContainerT, FailureT> liftToContainerValidatorType(
            SimpleValidator<MemberT, FailureT> memberValidation,
            Function1<ContainerT, MemberT> toMemberMapper,
            FailureT invalidContainer, FailureT invalidMember) {
        return validatedContainer -> {
            if (validatedContainer == null) {
                return invalidContainer;
            } else {
                val member = toMemberMapper.apply(validatedContainer);
                return member == null ? invalidMember : memberValidation.apply(member);
            }
        };
    }

    /**
     * Lifts a member validation to container type.
     * IMP: This doesn't do a null check on member. If the Member is null, the validation throws a NPE while executing.
     *      So the member validation is supposed to take that responsibility to check for null.
     *      This is specific to validations which want to check other params, based on member being null.  
     * @param memberValidation
     * @param toMemberMapper Mapper function to extract member from container
     * @param invalidContainer Failure to return if container is null
     * @param <ContainerT>
     * @param <MemberT>
     * @param <FailureT>
     * @return  container type validation
     */
    public static <ContainerT, MemberT, FailureT> SimpleValidator<ContainerT, FailureT> liftToContainerValidatorType(
            SimpleValidator<MemberT, FailureT> memberValidation,
            Function1<ContainerT, MemberT> toMemberMapper,
            FailureT invalidContainer) {
        return validatedContainer -> (validatedContainer == null) ? invalidContainer : memberValidation.apply(toMemberMapper.apply(validatedContainer));
    }
}
