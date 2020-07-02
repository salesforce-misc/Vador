/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus.dsl;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.types.validators.Validator;

import java.util.Objects;

/**
 * DSL to lift child validations to parent type.
 * 
 *  @author gakshintala
 *  @since 228
 */
@UtilityClass
public class ParentChildDsl {

    /**
     * Lifts a list of child validations to parent type.
     * @param childValidations  List of child validations
     * @param toChildMapper     Mapper function to extract child from parent
     * @param invalidParent     Failure to return if parent is null
     * @param invalidChild      Failure to return if child is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return                  List of parent type validations
     */
    public static <ParentT, ChildT, FailureT> List<Validator<ParentT, FailureT>> liftAllToParentValidationType(
            List<Validator<ChildT, FailureT>> childValidations,
            Function1<ParentT, ChildT> toChildMapper, FailureT invalidParent, FailureT invalidChild) {
        return childValidations.map(childValidation ->
                liftToParentValidationType(childValidation, toChildMapper, invalidParent, invalidChild));
    }

    /**
     * Lifts a list of child validations to parent type.
     * IMP: This doesn't do a null check on child, so the child validation is supposed to take that responsibility.
     * @param childValidations  List of child validations
     * @param toChildMapper     Mapper function to extract child from parent
     * @param invalidParent     Failure to return if parent is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return                  List of parent type validations
     */
    public static <ParentT, ChildT, FailureT> List<Validator<ParentT, FailureT>> liftAllToParentValidationType(
            List<Validator<ChildT, FailureT>> childValidations,
            Function1<ParentT, ChildT> toChildMapper, FailureT invalidParent) {
        return childValidations.map(childValidation ->
                liftToParentValidationType(childValidation, toChildMapper, invalidParent));
    }

    /**
     * Lifts a child validation to parent type.
     * @param childValidation
     * @param toChildMapper Mapper function to extract child from parent
     * @param invalidParent Failure to return if parent is null
     * @param invalidChild  Failure to return if child is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return  parent type validation
     */
    public static <ParentT, ChildT, FailureT> Validator<ParentT, FailureT> liftToParentValidationType(
            Validator<ChildT, FailureT> childValidation,
            Function1<ParentT, ChildT> toChildMapper,
            FailureT invalidParent, FailureT invalidChild) {
        return validatedParent -> {
            final Either<FailureT, ChildT> child = extractChild(toChildMapper, invalidParent, validatedParent);
            return child
                    .filter(Objects::nonNull)
                    .map(childValidation)
                    .getOrElse(Either.left(invalidChild));
        };
    }

    private static <ParentT, ChildT, FailureT> Either<FailureT, ChildT> extractChild(
            Function1<ParentT, ChildT> toChildMapper,
            FailureT invalidParent,
            Either<FailureT, ParentT> validatedParent) {
        return validatedParent
                .flatMap(parent -> parent == null ? Either.left(invalidParent) : Either.right(parent))
                .map(toChildMapper);
    }

    /**
     * Lifts a child validation to parent type.
     * IMP: This doesn't do a null check on child, so the child validation is supposed to take that responsibility.
     * @param childValidation
     * @param toChildMapper Mapper function to extract child from parent
     * @param invalidParent Failure to return if parent is null
     * @param <ParentT>
     * @param <ChildT>
     * @param <FailureT>
     * @return  parent type validation
     */
    public static <ParentT, ChildT, FailureT> Validator<ParentT, FailureT> liftToParentValidationType(
            Validator<ChildT, FailureT> childValidation,
            Function1<ParentT, ChildT> toChildMapper,
            FailureT invalidParent) {
        return validatedParent -> childValidation.apply(extractChild(toChildMapper, invalidParent, validatedParent));
    }
}
