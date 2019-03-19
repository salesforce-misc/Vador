package failure;/*
 * Copyright 2018 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */


/**
 * Interface for validation failures across all services
 *
 * @author gakshintala
 * @since 220
 */
public interface ValidationFailure {
    String getErrorMessage();

    void setErrorMessage(String errorMessage);

    ValidationFailure withErrorMessage(String errorMessage);

    ValidationFailure withPlaceHolders(String... placeHolders);
    
}
