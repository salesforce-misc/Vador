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
    /**
     * Capture service section
     */
    AUTH_NOT_PROCESSED(Section.CAPTURE_VALIDATION_FAILURE, "AuthNotProcessed"),
    AUTH_GATEWAY_ID_ABSENT(Section.CAPTURE_VALIDATION_FAILURE, "AuthGatewayIdAbsent"),

    /**
     * Payment entity section
     */
    AMOUNT_NOT_EQUALS_BALANCE(Section.PAYMENT_ENTITY_VALIDATION_FAILURE, "ColumnValuesMismatch"),
    CURRENCY_CODE_MISMATCH_BETWEEN_GROUP_AND_AUTH(Section.PAYMENT_ENTITY_VALIDATION_FAILURE, "DifferentCurrencyCodeForGroupAndAuth"),
    CURRENCY_CODE_MISMATCH_PAYMENT(Section.PAYMENT_ENTITY_VALIDATION_FAILURE, "CurrencyCodeMismatchForPayment"),
    AUTH_ONLY_IN_CAPTURE(Section.PAYMENT_ENTITY_VALIDATION_FAILURE, "AuthOnlyInCapture"),
    AUTH_NOT_PROCESSED_ENTITY_VALIDATION(Section.PAYMENT_ENTITY_VALIDATION_FAILURE, "AuthNotProcessed"),

    /**
     * Auth service validation
     */
    GATEWAY_INSTANCE_NOT_ACTIVE(Section.AUTHORIZATION_VALIDATION_FAILURE, "GatewayInstanceNotActive"),
    INVALID_GATEWAY_INSTANCE_PM(Section.AUTHORIZATION_VALIDATION_FAILURE, "GatewayInstanceNotValidInPM"),
    GATEWAY_INSTANCE_PM_NOT_FOUND(Section.AUTHORIZATION_VALIDATION_FAILURE, "GatewayInstanceNotPresentInPM"),
    CURRENCY_CODE_MISMATCH(Section.AUTHORIZATION_VALIDATION_FAILURE, "CurrencyCodeMismatch"),
    
    /**
     * Refund service validation.
     */
    PAYMENT_NOT_PROCESSED_FOR_REFUND(Section.REFUND_VALIDATION_FAILURE, "PaymentNotProcessed"),
    PAYMENT_GATEWAY_ID_ABSENT(Section.REFUND_VALIDATION_FAILURE, "PaymentGatewayIdAbsent"),
    REFUND_AMOUNT_EXCEEDS_PAYMENT_BALANCE(Section.REFUND_VALIDATION_FAILURE, "RefundAmountExceedsPaymentBalance"),
    REFUND_AMOUNT_NOT_EQUALS_BALANCE(Section.REFUND_VALIDATION_FAILURE, "AmountBalanceMisMatch"),

    
    /**
     * Common validation section
     */
    SUCCESS(Section.COMMON_VALIDATION_FAILURE, "Success"),
    NOTHING_TO_VALIDATE(Section.COMMON_VALIDATION_FAILURE, "Nothing"),
    UNKNOWN_EXCEPTION("", ""), // This is used as a placeholder for ValidationFailureMessage during RuntimeExceptions.
    FIELD_ABSENT(Section.COMMON_VALIDATION_FAILURE, "RequiredFieldMissing"),
    FIELD_NULL_OR_EMPTY(Section.COMMON_VALIDATION_FAILURE, "FieldNullOrEmpty"),
    FIELD_NEGATIVE(Section.COMMON_VALIDATION_FAILURE, "FieldNegative"),
    INVALID_FOREIGN_KEY_VALUE(Section.COMMON_VALIDATION_FAILURE, "InvalidForeignKeyValue"),
    INSUFFICIENT_ACCESS_RIGHTS_FOR_ID(Section.COMMON_VALIDATION_FAILURE, "InsufficientAccessRightsForId"),
    INSUFFICIENT_ACCESS_RIGHTS_FOR_ENTITYID(Section.COMMON_VALIDATION_FAILURE, "InsufficientAccessRightsForEntityId"),
    CONFLICTING_FIELD(Section.COMMON_VALIDATION_FAILURE, "ConflictingFields"),
    ONE_FIELD_REQUIRED(Section.COMMON_VALIDATION_FAILURE, "OneFieldRequired"),
    STATUS_NOT_ALLOWED(Section.COMMON_VALIDATION_FAILURE, "StatusNotAllowed"),
    STATUS_NOT_ALLOWED_IN_MODE(Section.COMMON_VALIDATION_FAILURE, "StatusNotAllowedInMode"),
    FIELD_IMMUTABLE_IN_STATUS(Section.COMMON_VALIDATION_FAILURE, "FieldImmutableInStatus"),
    INVALID_FIELD_VALUE(Section.COMMON_VALIDATION_FAILURE, "InvalidFieldValue"),
    FIELD_IMMUTABLE(Section.COMMON_VALIDATION_FAILURE, "FieldImmutable"),
    DELETE_NOT_ALLOWED(Section.COMMON_VALIDATION_FAILURE, "DeleteNotAllowed"),
    MAX_LENGTH_LIMIT_EXCEEDED(Section.COMMON_VALIDATION_FAILURE, "MaxLengthLimitExceeded"),
    INVALID_EMAIL(Section.COMMON_VALIDATION_FAILURE, "InvalidEmail"),
    UNSUPPORTED_CURRENCY(Section.COMMON_VALIDATION_FAILURE, "CurrencyNotSupported"),
    CURRENCY_MISMATCH(Section.COMMON_VALIDATION_FAILURE, "CurrencyMismatch"),
    INVALID_INET_ADDRESS(Section.COMMON_VALIDATION_FAILURE, "InvalidInetAddress"),
    INVALID_NUMBER(Section.COMMON_VALIDATION_FAILURE, "InvalidNumber"),
    INVALID_STATE(Section.COMMON_VALIDATION_FAILURE, "InvalidState"),
    INVALID_COUNTRY(Section.COMMON_VALIDATION_FAILURE, "InvalidCountry"),
    STATE_WITHOUT_COUNTRY(Section.COMMON_VALIDATION_FAILURE, "StateWithoutCountryNotAllowed"),
    REQUIRED_FIELD_MISSING_IN_NODE(Section.COMMON_VALIDATION_FAILURE, "RequiredFieldMissingInNode"),
    INVALID_PICKLIST_VALUE(Section.COMMON_VALIDATION_FAILURE, "InvalidPicklistValue"),
    ADDITIONAL_DATA_SIZE_EXCEEDS(Section.COMMON_VALIDATION_FAILURE, "AdditionalDataSizeExceeds"),
    ADDITIONAL_DATA_KEY_LENGTH_EXCEEDS(Section.COMMON_VALIDATION_FAILURE, "AdditionalDataKeyLengthExceeds"),
    ADDITIONAL_DATA_VALUE_LENGTH_EXCEEDS(Section.COMMON_VALIDATION_FAILURE, "AdditionalDataValueLengthExceeds"),
    
    /**
     * Payment Method validation section.
     */
    // TODO: 2019-02-15 seperate out card payment method labels to separate file.
    PAYMENT_METHOD_NO_ID_OR_DETAILS(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "NoIdOrDetails"),
    PAYMENT_METHOD_ID_AND_DETAILS(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "IdAndDetails"),
    PAYMENT_METHOD_MULTIPLE_DETAILS(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "MultipleDetails"),
    PAYMENT_METHOD_INACTIVE(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "Inactive"),
    PAYMENT_METHOD_DATE_FIELDS_INVALID_WITH_ALL_FIELDS(Section.PAYMENT_METHOD_VALIDATION_FAILURE,
                                                       "DateFieldsInvalidWithAllFields"),
    PAYMENT_METHOD_DATE_FIELDS_INVALID_COMBINATION(Section.PAYMENT_METHOD_VALIDATION_FAILURE,
                                                   "DateFieldsInvalidCombination"),
    PAYMENT_METHOD_DATE_FIELDS_INVALID_MONTH(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "DateFieldsInvalidMonth"),
    PAYMENT_METHOD_DATE_FIELDS_INVALID_YEAR(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "DateFieldsInvalidYear"),
    PAYMENT_METHOD_GATEWAY_TOKEN_MISSING(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "GatewayTokenMissing"),
    PAYMENT_METHOD_INVALID_CARD_EXTERNAL(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "InvalidCard_ExternalMode"),
    PAYMENT_METHOD_INVALID_CARD_SALESFORCE(Section.PAYMENT_METHOD_VALIDATION_FAILURE, "InvalidCard_SalesForceMode"),
    /**
     * Common line validations
     */
    INVALID_UNAPPLICATIONSTATUS(Section.LINE_VALIDATION_FAILURE, "InvalidValueForUnapplicationStatus"),
    PAYMENT_NOT_PROCESSED(Section.LINE_VALIDATION_FAILURE, "PaymentNotProcessed"),
    FIELD_NOT_NULL(Section.LINE_VALIDATION_FAILURE, "FieldNotNull"),
    AMOUNT_GREATER_THAN_PAYMENTBALANCE(Section.LINE_VALIDATION_FAILURE, "AmountGreaterThanPaymentBalance"),

    /**
     * PaymentLineInvoice section
     */
    INVALID_CURRENCY_ISO_CODE(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "InvalidCurrencyIsoCode"),
    INVOICE_NOT_POSTED(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "InvoiceNotPosted"),
    RECORD_LOCKED(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "RecordsLocked"),
    AMOUNT_GREATER_THAN_INVOICEBALANCE(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "AmountGreaterThanInvoiceBalance"),
    VALUE_DIFFERENT_FROM_ASSOCIATEDPL(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "DifferentFromAssociatedPaymentLine"),
    ASSOCIATED_PL_NULL(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "AssociatedPaymentLineNull"),
    ASSOCIATE_PL_TYPE_ERROR(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "AssociatedPaymentLineTypeValidation"),
    ASSOCIATED_PL_UNAPPLIED(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "AssociatedPaymentLineIsUnapplied"),
    ASSOCIATED_PL_UNAPPLIED_DATE(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "AssociatedPaymentLineAppliedDateAfterUnappliedDate"),
    DUPLICATE_REQUEST_FOR_UNAPPLY(Section.PAYMENT_LINE_INVOICE_VALIDATION_FAILURE, "DuplicateUnapplyRequest"),
    
    /**
     * RefundLinePayment section
     */
    RLP_INVALID_CURRENCY_ISO_CODE(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "InvalidCurrencyIsoCode"),
    RLP_REFUND_NOT_PROCESSED(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "RefundNotProcessed"),
    RLP_AMOUNT_GREATER_THAN_REFUND_BALANCE(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "AmountGreaterThanRefundBalance"),
    RLP_BULK_APPLY_BLOCKED(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "bulkBlocked"),
    RLP_RECORDS_LOCKED(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "refundLinePaymentRecordsLocked"),
    RLP_BALANCE_SNAPSHOT_FAILURE(Section.REFUND_LINE_PAYMENT_VALIDATION_FAILURE, "BalanceSnapShotFailure"),

    
    /**
     * PaymentGatewayProvider section
     */
    UNIQUE_CONSTRAINT_ON_APEX_ADAPTER(Section.PAYMENT_GATEWAY_PROVIDER_VALIDATION_FAILURE, "UniqueConstraintOnApexAdapter"),

    /**
     * PaymentGateway section
     */
    UNIQUE_CONSTRAINT_ON_PAYMENT_GATEWAY(Section.PAYMENT_GATEWAY_VALIDATION_FAILURE, "UniqueConstraintOnPaymentGateway"),
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
        static final String CAPTURE_VALIDATION_FAILURE = "CaptureValidationFailure";
        static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
        static final String PAYMENT_ENTITY_VALIDATION_FAILURE = "PaymentEntityValidationFailure";
        static final String AUTHORIZATION_VALIDATION_FAILURE = "AuthorizationValidationFailure";
        static final String PAYMENT_METHOD_VALIDATION_FAILURE = "PaymentMethodValidationFailure";
        static final String PAYMENT_LINE_INVOICE_VALIDATION_FAILURE = "PaymentLineInvoiceValidationFailure";
        static final String PAYMENT_GATEWAY_PROVIDER_VALIDATION_FAILURE = "PaymentGatewayProviderValidationFailure";
        static final String PAYMENT_GATEWAY_VALIDATION_FAILURE = "PaymentGatewayValidationFailure";
        static final String REFUND_VALIDATION_FAILURE = "RefundValidationFailure";
        static final String REFUND_LINE_PAYMENT_VALIDATION_FAILURE = "RefundLinePaymentValidationFailure";
        static final String LINE_VALIDATION_FAILURE = "LineValidationFailure";

    }
}
