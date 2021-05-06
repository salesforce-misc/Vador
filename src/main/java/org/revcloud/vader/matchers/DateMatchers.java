package org.revcloud.vader.matchers;

import io.vavr.Function2;
import lombok.experimental.UtilityClass;

import java.util.Date;

@UtilityClass
public class DateMatchers {
    @SuppressWarnings({"java:S1874"})
    public static Function2<Object, Object, Boolean> isEqualToDayOfDate() {
        return (day, date) -> {
            if (day == null && date == null) {
                return false;
            }
            if (!(day instanceof Integer) || !(date instanceof Date)) {
                return false;
            }
            return (Integer) day == ((Date) date).getDate();
        };
    }

    public static Function2<Object, Object, Boolean> isOnOrBeforeIfBothArePresent() {
        return (date1, date2) -> {
            if (date1 == null || date2 == null) {
                return true;
            }
            if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
                return false;
            }
            return date1.equals(date2) || ((Date) date1).before((Date) date2);
        };
    }

    public static Function2<Object, Object, Boolean> isBeforeIfBothArePresent() {
        return (date1, date2) -> {
            if (date1 == null && date2 == null) {
                return true;
            }
            if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
                return false;
            }
            return ((Date) date1).before((Date) date2);
        };
    }
}
