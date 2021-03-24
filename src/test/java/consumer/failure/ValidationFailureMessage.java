/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package consumer.failure;


import java.util.Objects;


/**
 * This enum holds all localized representations of all Service validation Failures.
 *
 * @author gakshintala
 * @since 220
 */

public enum ValidationFailureMessage {
    SUCCESS(Section.COMMON_VALIDATION_FAILURE, "Success"),
    FIELD_NULL_OR_EMPTY("", ""),
    NOTHING_TO_VALIDATE(Section.COMMON_VALIDATION_FAILURE, "Nothing"),
    INVALID_PARENT(Section.COMMON_VALIDATION_FAILURE, "invalidParent"),
    INVALID_CHILD(Section.COMMON_VALIDATION_FAILURE, "invalidChild"),
    UNKNOWN_EXCEPTION("", ""), 
    VALIDATION_FAILURE_1("", ""),
    VALIDATION_FAILURE_2("", ""),
    VALIDATION_FAILURE_3("", ""),
    REQUIRED_FIELD_MISSING("", ""),
    FIELD_INTEGRITY_EXCEPTION("", ""),
    ;
    
    private final String section;
    private final String name;

    ValidationFailureMessage(String section, String name) {
        Objects.requireNonNull(section, "section for ValidationFailure cannot be null");
        Objects.requireNonNull(name, "name for ValidationFailure cannot be null");
        this.section = section;
        this.name = name;
    }

    public String getSection() {
        return section;
    }

    public String getName() {
        return name;
    }

    private static final class Section {
        static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
    }

    public String getMessage() {
        return section + " " + name;
    }
}
