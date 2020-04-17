/*
 * Copyright, 1999-2019, salesforce.com All Rights Reserved Company Confidential
 */
package consumer.failure;


/**
 * Exception class extending SalesforceRuntime exception. This should be used to show proper error message/code to the
 * user
 *
 * @author r.shankar
 * @since 220
 */
public class PaymentsRuntimeException extends RuntimeException {

    private ApiErrorCodes codes;

    public PaymentsRuntimeException(ApiErrorCodes codes, String message) {
        super(message);
        this.codes = codes;
    }
    
    public PaymentsRuntimeException(ApiErrorCodes codes) {
        super();
        this.codes = codes;
    }
    
    /**
     * @return Returns the codes.
     */
    public ApiErrorCodes getCodes() {
        return this.codes;
    }
}
