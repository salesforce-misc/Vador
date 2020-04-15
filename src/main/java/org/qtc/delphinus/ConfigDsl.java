/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.function.Function;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Holds utility methods for Validation.
 * <p>
 * gakshintala created on 2/22/20.
 */
public final class ConfigDsl {

    private ConfigDsl() {
    }

    /**
     * Method to get the group of shared validations for a particular shared representation.
     * This method should be called on the instance of Shared Input representation.
     *
     * @param toSharedRepresentationMapper Function that can map ParentInputRepresentation to SharedInputRepresentation.
     * @param <ParentT> Parent representation type to be validated. Parent HAS-A shared representation.
     * @return Composition of all shared validations for a shared representation.
     */
    public static <ParentT, ChildT, FailureT>
    ImmutableList<RequestValidation<FailureT, ParentT>> liftAllToParentRequestValidationType(
            ImmutableList<RequestValidation<FailureT, ChildT>> sharedInputRequestValidations,
            Function<ParentT, ChildT> toSharedRepresentationMapper,
            FailureT invalidChild) {
        return sharedInputRequestValidations.stream()
                .map(requestValidation -> liftToParentRequestValidationType(requestValidation, toSharedRepresentationMapper, invalidChild))
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }

    /**
     * This method converts a sharedValidation to Parent request validation.
     *
     * @param sharedRequestValidation      shared request validation to be converted.
     * @param toSharedRepresentationMapper Function that can map ParentInputRepresentation to SharedInputRepresentation.
     * @param <ParentT> Parent representation type to be validated. Parent HAS-A shared representation.
     * @return converted parent request validator.
     */
    public static <ParentT, ChildT, FailureT>
    RequestValidation<FailureT, ParentT>
    liftToParentRequestValidationType(RequestValidation<FailureT, ChildT> sharedRequestValidation,
                                      Function<ParentT, ChildT> toSharedRepresentationMapper,
            FailureT invalidChild) {
        return parentInputRepresentation -> {
            final ChildT sharedInputRepresentation = toSharedRepresentationMapper.apply(
                    parentInputRepresentation);
            return sharedInputRepresentation != null
                    ? sharedRequestValidation.validate(sharedInputRepresentation)
                    : invalidChild;
        };
    }

    /**
     * Concatinates and flattens groups of Request validations into single group.
     *
     * @param validationGroups Validation groups to be concatenated.
     * @return Concatenated and flattened group of request validations.
     */
    public static <FailureT, InputRepresentationT> ImmutableList<RequestValidation<FailureT, InputRepresentationT>> concatValidationGroups(
            ImmutableList<ImmutableList<RequestValidation<FailureT, InputRepresentationT>>> validationGroups) {
        return validationGroups.stream()
                .flatMap(Collection::stream).collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }
}
