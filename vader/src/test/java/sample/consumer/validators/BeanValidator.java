package sample.consumer.validators;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import lombok.Value;
import org.revcloud.vader.types.Validator;
import org.revcloud.vader.types.ValidatorEtr;
import sample.consumer.failure.ValidationFailure;

/** Sample Validators for some random Bean */
public class BeanValidator {

  public static final Validator<Bean, ValidationFailure> validator =
      bean -> {
        if (bean == null) {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        } else {
          return ValidationFailure.NONE;
        }
      };

  public static final ValidatorEtr<Bean, ValidationFailure> validatorEtr =
      beanEtr ->
          beanEtr.filterOrElse(
              bean -> bean.id != null, badBean -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

  @Value
  private static class Bean {
    String id;
  }
}
