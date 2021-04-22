package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import consumer.failure.ValidationFailureMessage;
import io.vavr.Function1;
import io.vavr.Tuple;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerDslWithConfigTest {

    // TODO 22/04/21 gopala.akshintala: Make sure all flows in all strategies are tested 
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

    @Test
    void failFastWithRequiredFieldsMissingForSimpleValidators() {
        val validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate()
                        .shouldHaveFields(List.of(
                                Tuple.of(Bean::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(Bean::getRequiredField2, REQUIRED_FIELD_MISSING)))
                        .shouldHaveValidSFIds(List.of(
                                Tuple.of(Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))).prepare();

        val validatableWithBlankReqField = new Bean(0, "", null, null);
        val result1 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithBlankReqField,
                throwable -> UNKNOWN_EXCEPTION,
                validationConfig
        );
        Assertions.assertSame(REQUIRED_FIELD_MISSING, result1);

        val validatableWithNullReqField = new Bean(0, null, null, null);
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
    void failFastWithRequiredFieldsWithNameMissingForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate()
                        .shouldHaveFieldsWithName(Tuple.of(Map.of(
                                Bean.Fields.requiredField1, Bean::getRequiredField1,
                                Bean.Fields.requiredField2, Bean::getRequiredField2),
                                (name, value) -> getFailureWithParams(ValidationFailureMessage.MSG_WITH_PARAMS, name, value)))
                        .prepare();
        val withRequiredFieldNull = new Bean(1, "", null, null);
        val result = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                withRequiredFieldNull,
                throwable -> UNKNOWN_EXCEPTION,
                validationConfig
        );
        assertThat(result.getValidationFailureMessage().getParams()).containsExactly(Bean.Fields.requiredField2, "");
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate()
                        .shouldHaveFields(List.of(
                                Tuple.of(Bean::getRequiredField1, REQUIRED_FIELD_MISSING),
                                Tuple.of(Bean::getRequiredField2, REQUIRED_FIELD_MISSING)))
                        .shouldHaveValidSFIds(List.of(
                                Tuple.of(Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION),
                                Tuple.of(Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))).prepare();

        val validatableWithInvalidSfId = new Bean(0, "1", new ID("1ttxx00000000hZAAQ"), new ID("invalidSfId"));
        val result1 = validateAndFailFastForSimpleValidatorsWithConfig(
                NONE,
                NOTHING_TO_VALIDATE,
                validatableWithInvalidSfId,
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        Assertions.assertSame(FIELD_INTEGRITY_EXCEPTION, result1);
    }

    @Value
    @FieldNameConstants
    private static class Bean {
        Integer requiredField1;
        String requiredField2;
        ID sfId1;
        ID sfId2;
    }
}
