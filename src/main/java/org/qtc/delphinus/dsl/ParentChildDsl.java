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
import lombok.val;
import org.qtc.delphinus.types.validators.Validator;

import java.util.Objects;

/**
 * gakshintala created on 4/15/20.
 */
@UtilityClass
public class ParentChildDsl {

    public static <ParentT, ChildT, FailureT> List<Validator<ParentT, FailureT>> liftAllToParentValidationType(
            List<Validator<ChildT, FailureT>> childValidations,
            Function1<ParentT, ChildT> toChildMapper, FailureT invalidParent, FailureT invalidChild) {
        return childValidations.map(childValidation ->
                liftToParentValidationType(childValidation, toChildMapper, invalidParent, invalidChild));
    }
    
    public static <ParentT, ChildT, FailureT> Validator<ParentT, FailureT> liftToParentValidationType(
            Validator<ChildT, FailureT> childValidation,
            Function1<ParentT, ChildT> toChildMapper, 
            FailureT invalidParent, FailureT invalidChild) {
        return validatedParent -> {
            val child = validatedParent
                    .flatMap(parent -> parent == null ? Either.left(invalidParent) : Either.right(parent))
                    .map(toChildMapper);
            return child
                    .filter(Objects::nonNull)
                    .map(childValidation)
                    .getOrElse(Either.left(invalidChild))
                    .flatMap(ignore -> validatedParent);
        };
    }
}
