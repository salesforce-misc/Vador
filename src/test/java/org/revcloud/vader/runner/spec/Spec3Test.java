package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revcloud.vader.matchers.DateMatchers.isBeforeIfBothArePresent;

class Spec3Test {
    @Test
    void testDates() {
        final var specName = "CompareDates";
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._3().nameForTest(specName)
                        .when(Bean::isCompareDates)
                        .matches(is(true))
                        .thenField1(Bean::getDate1)
                        .thenField2(Bean::getDate2)
                        .shouldRelateWithFn(isBeforeIfBothArePresent())).prepare();

        final var validBean = new Bean(true, new GregorianCalendar(2021, Calendar.APRIL, 27).getTime(),
                new GregorianCalendar(2021, Calendar.APRIL, 28).getTime());
        assertTrue(validationConfig.getSpecWithName(specName).map(spec -> spec.test(validBean)).orElse(false));

        final var invalidBean = new Bean(true, new GregorianCalendar(2021, Calendar.APRIL, 29).getTime(),
                new GregorianCalendar(2021, Calendar.APRIL, 28).getTime());
        assertFalse(validationConfig.getSpecWithName(specName).map(spec -> spec.test(invalidBean)).orElse(true));
    }

    @Test
    void testDatesWithNulls() {
        final var specName = "CompareDates";
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._3().nameForTest(specName)
                        .when(Bean::isCompareDates)
                        .matches(is(true))
                        .thenField1(Bean::getDate1)
                        .thenField2(Bean::getDate2)
                        .shouldRelateWithFn(isBeforeIfBothArePresent())).prepare();
        final var invalidBean1 = new Bean(true, null, new GregorianCalendar(2021, Calendar.APRIL, 29).getTime());
        assertFalse(validationConfig.getSpecWithName(specName).map(spec -> spec.test(invalidBean1)).orElse(true));

        final var invalidBean2 = new Bean(true, new GregorianCalendar(2021, Calendar.APRIL, 29).getTime(), null);
        assertFalse(validationConfig.getSpecWithName(specName).map(spec -> spec.test(invalidBean2)).orElse(true));
    }

    @Value
    private static class Bean {
        boolean compareDates;
        Date date1;
        Date date2;
    }
}
