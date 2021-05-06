/*
 * Copyright 2021 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.vader.matchers;

import lombok.experimental.UtilityClass;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.IsNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class AnyMatchers {
    @SafeVarargs
    public static <T> AnyOf<T> anyOf(T... ts) {
        return Matchers.anyOf(Arrays.stream(ts).map(Matchers::is).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T> AnyOf<T> anyOf(Matcher<T>... ts) {
        return Matchers.anyOf(Arrays.stream(ts).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T> AnyOf<T> anyOfOrNull(Matcher<T>... ts) {
        return Matchers.anyOf(Stream.of(Arrays.stream(ts), Stream.<Matcher<T>>of(new IsNull<T>())).flatMap(Function.identity()).collect(Collectors.toList()));
    }

}
