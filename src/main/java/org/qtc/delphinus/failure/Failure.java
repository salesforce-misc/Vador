package org.qtc.delphinus.failure;

import org.qtc.delphinus.FailureMessageDetails;

/**
 * gakshintala created on 4/14/20.
 */
public interface Failure {
    FailureMessageDetails getFailureMessageDetails();

    String getExceptionMessageFromFailure();
}
