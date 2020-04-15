/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

/**
 * gakshintala created on 4/9/20.
 */
public class FailureMessageDetails {
    private final String section;
    private final String name;

    public FailureMessageDetails(String section, String name) {
        this.section = section;
        this.name = name;
    }

    public String getSection() {
        return section;
    }

    public String getName() {
        return name;
    }
}
