package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.bean.BaseParent;
import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.config.ValidationConfig;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import static consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

class RunnerDslTest {

    @Test
    void failFastWithFirstFailure() {
        val result = validateAndFailFast(
                NONE,
                NOTHING_TO_VALIDATE,
                new BaseParent(0, null, null),
                UNKNOWN_EXCEPTION
        );
        Assertions.assertSame(UNKNOWN_EXCEPTION, result);
    }

    private static <ParentT, FailureT> FailureT validateAndFailFast(
            FailureT none,
            FailureT nothingToValidate,
            ParentT parentToValidate,
            FailureT firstValidationFailure
    ) {

        Validator<ParentT, FailureT> v1 = parent -> Either.right(none);
        Validator<ParentT, FailureT> v2 = parent -> Either.right(none);
        Validator<ParentT, FailureT> v3 = parent -> Either.left(firstValidationFailure);

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFast(
                parentToValidate,
                validationList,
                nothingToValidate,
                none,
                throwable -> none
        );
    }

    @Test
    void failFastWithFirstFailureForSimpleValidators() {
        final var validatable = new BaseParent(0, null, null);
        val result = validateAndFailFastForSimpleValidators(
                NONE,
                NOTHING_TO_VALIDATE,
                validatable,
                UNKNOWN_EXCEPTION,
                throwable -> UNKNOWN_EXCEPTION
        );
        Assertions.assertSame(UNKNOWN_EXCEPTION, result);
    }

    private static <ValidatableT, FailureT> FailureT validateAndFailFastForSimpleValidators(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            FailureT firstValidationFailure,
            Function1<Throwable, FailureT> throwableMapper
    ) {

        SimpleValidator<ValidatableT, FailureT> v1 = parent -> none;
        SimpleValidator<ValidatableT, FailureT> v2 = parent -> none;
        SimpleValidator<ValidatableT, FailureT> v3 = parent -> firstValidationFailure;

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                validatable,
                validationList,
                nothingToValidate,
                none,
                throwableMapper
        );
    }

    @Test
    void failFastWithRequiredFieldMissingForSimpleValidators() {
        ValidationConfig<BaseParent, ValidationFailure> validationConfig =
                ValidationConfig.toValidate(BaseParent.class, ValidationFailure.class)
                        .shouldHaveRequiredFields(
                                Tuple.of(BaseParent::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(BaseParent::getRequiredField2, REQUIRED_FIELD_MISSING),
                                Tuple.of(BaseParent::getRequiredField3, REQUIRED_FIELD_MISSING))
                        .shouldHaveValidSFIds(
                                Tuple.of(BaseParent::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(BaseParent::getSfId2, FIELD_INTEGRITY_EXCEPTION));
        
        final var validatableWithBlankReqField = new BaseParent(0, null, null, 1, "", null, null, null);
        val result1 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithBlankReqField,
                throwable -> UNKNOWN_EXCEPTION,
                validationConfig
        );
        Assertions.assertSame(REQUIRED_FIELD_MISSING, result1);

        final var validatableWithNullReqField = new BaseParent(0, null, null, 1, "str", null, null, null);
        val result2 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithNullReqField,
                throwable -> UNKNOWN_EXCEPTION,
                validationConfig
        );
        Assertions.assertSame(REQUIRED_FIELD_MISSING, result2);
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<BaseParent, ValidationFailure> validationConfig =
                ValidationConfig.toValidate(BaseParent.class, ValidationFailure.class)
                        .shouldHaveRequiredFields(
                                Tuple.of(BaseParent::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(BaseParent::getRequiredField2, REQUIRED_FIELD_MISSING),
                                Tuple.of(BaseParent::getRequiredField3, REQUIRED_FIELD_MISSING))
                        .shouldHaveValidSFIds(
                                Tuple.of(BaseParent::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(BaseParent::getSfId2, FIELD_INTEGRITY_EXCEPTION));

        final var validatableWithInvalidSfId = new BaseParent(0, null, null, 1, "str", "str", new ID("1ttxx00000000hZAAQ"), new ID("invalidSfId"));
        val result1 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithInvalidSfId,
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        Assertions.assertSame(FIELD_INTEGRITY_EXCEPTION, result1);
    }

    private static <ValidatableT, FailureT> FailureT validateAndFailFastForSimpleValidatorsWithConfig(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig
    ) {

        SimpleValidator<ValidatableT, FailureT> v1 = parent -> none;
        SimpleValidator<ValidatableT, FailureT> v2 = parent -> none;

        val validationList = List.of(v1, v2);
        
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                validatable,
                validationList,
                nothingToValidate,
                none,
                throwableMapper,
                validationConfig
        );
    }

}

