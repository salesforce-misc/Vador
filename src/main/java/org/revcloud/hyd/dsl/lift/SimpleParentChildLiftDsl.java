/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.hyd.dsl.lift;

import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.hyd.types.validators.SimpleValidator;

/**
 * DSL to lift simple child validations to parent type.
 * 
 *  @author gakshintala
 *  @since 228
 */
@UtilityClass
public class SimpleParentChildLiftDsl {

    /**
     * Lifts a list of simple child validations to parent type.
     * @param childValidations  List of child validations
     * @param toChildMapper     Mapper function to extract child from parent
     * @param invalidParent     Failure to return if parent is null
     * @param invalidChild      Failure to return if child is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return                  List of parent type validations
     */
    public static <ParentT, ChildT, FailureT> List<SimpleValidator<ParentT, FailureT>> liftAllToParentValidationType(
            List<SimpleValidator<ChildT, FailureT>> childValidations,
            Function1<ParentT, ChildT> toChildMapper, FailureT invalidParent, FailureT invalidChild) {
        return childValidations.map(childValidation ->
                liftToParentValidationType(childValidation, toChildMapper, invalidParent, invalidChild));
    }

    /**
     * Lifts a list of simple child validations to parent type.
     * IMP: This doesn't do a null check on child, so the child validation is supposed to take that responsibility.
     * @param childValidations  List of child validations
     * @param toChildMapper     Mapper function to extract child from parent
     * @param invalidParent     Failure to return if parent is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return                  List of parent type validations
     */
    public static <ParentT, ChildT, FailureT> List<SimpleValidator<ParentT, FailureT>> liftAllToParentValidationType(
            List<SimpleValidator<ChildT, FailureT>> childValidations,
            Function1<ParentT, ChildT> toChildMapper, FailureT invalidParent) {
        return childValidations.map(childValidation ->
                liftToParentValidationType(childValidation, toChildMapper, invalidParent));
    }

    /**
     * Lifts a simple child validation to parent type.
     * @param childValidation
     * @param toChildMapper Mapper function to extract child from parent
     * @param invalidParent Failure to return if parent is null
     * @param invalidChild  Failure to return if child is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return  parent type validation
     */
    public static <ParentT, ChildT, FailureT> SimpleValidator<ParentT, FailureT> liftToParentValidationType(
            SimpleValidator<ChildT, FailureT> childValidation,
            Function1<ParentT, ChildT> toChildMapper,
            FailureT invalidParent, FailureT invalidChild) {
        return validatedParent -> {
            if (validatedParent == null) {
                return invalidParent;
            } else {
                val child = toChildMapper.apply(validatedParent);
                return child == null ? invalidChild : childValidation.apply(child);
            }
        };
    }

    /**
     * Lifts a child validation to parent type.
     * IMP: This doesn't do a null check on child. If the Child is null, the validation throws a NPE while executing.
     *      So the child validation is supposed to take that responsibility to check for null.
     *      This is specific to validations which want to check other params, based on child being null.  
     * @param childValidation
     * @param toChildMapper Mapper function to extract child from parent
     * @param invalidParent Failure to return if parent is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return  parent type validation
     */
    public static <ParentT, ChildT, FailureT> SimpleValidator<ParentT, FailureT> liftToParentValidationType(
            SimpleValidator<ChildT, FailureT> childValidation,
            Function1<ParentT, ChildT> toChildMapper,
            FailureT invalidParent) {
        return validatedParent -> (validatedParent == null) ? invalidParent : childValidation.apply(toChildMapper.apply(validatedParent));
    }
}
