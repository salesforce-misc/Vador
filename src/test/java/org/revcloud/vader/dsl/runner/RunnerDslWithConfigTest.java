package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import io.vavr.Tuple;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

class RunnerDslWithConfigTest {

    @Test
    void failFastWithRequiredFieldMissingForSimpleValidators() {
        ValidationConfig<Parent, ValidationFailure> validationConfig =
                ValidationConfig.<Parent, ValidationFailure>toValidate()
                        .shouldHaveFields(List.of(
                                Tuple.of(Parent::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(Parent::getRequiredField2, REQUIRED_FIELD_MISSING),
                                Tuple.of(Parent::getRequiredField3, REQUIRED_FIELD_MISSING)))
                        .shouldHaveValidSFIds(List.of(
                                Tuple.of(Parent::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(Parent::getSfId2, FIELD_INTEGRITY_EXCEPTION))).prepare();

        val validatableWithBlankReqField = new Parent(0, null, null, 1, "", null, null, null);
        val result1 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithBlankReqField,
                throwable -> UNKNOWN_EXCEPTION,
                validationConfig
        );
        Assertions.assertSame(REQUIRED_FIELD_MISSING, result1);

        val validatableWithNullReqField = new Parent(0, null, null, 1, "str", null, null, null);
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
        ValidationConfig<Parent, ValidationFailure> validationConfig =
                ValidationConfig.<Parent, ValidationFailure>toValidate()
                        .shouldHaveFields(List.of(
                                Tuple.of(Parent::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(Parent::getRequiredField2, REQUIRED_FIELD_MISSING),
                                Tuple.of(Parent::getRequiredField3, REQUIRED_FIELD_MISSING)))
                        .shouldHaveValidSFIds(List.of(
                                Tuple.of(Parent::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(Parent::getSfId2, FIELD_INTEGRITY_EXCEPTION))).prepare();

        val validatableWithInvalidSfId = new Parent(0, null, null, 1, "str", "str", new ID("1ttxx00000000hZAAQ"), new ID("invalidSfId"));
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
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                validatable,
                io.vavr.collection.List.empty(),
                nothingToValidate,
                none,
                throwableMapper,
                validationConfig);
    }
}
