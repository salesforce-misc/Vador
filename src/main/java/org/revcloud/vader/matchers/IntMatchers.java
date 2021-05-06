package org.revcloud.vader.matchers;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@UtilityClass
public class IntMatchers {
    public static Matcher<Integer> inRangeInclusive(int start, int end) {
        return allOf(greaterThanOrEqualTo(start), lessThanOrEqualTo(end));
    }
}
