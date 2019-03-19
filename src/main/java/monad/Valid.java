package monad;/*
 * Copyright 2018 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */


import failure.ValidationFailure;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Monad to hold valid values
 *
 * @author gakshintala
 * @since 220
 */
public class Valid<T> implements MaybeValid<T> {

    private final T value;
    private static final MaybeValid<?> EMPTY = new Valid<>();

    public Valid(T value) {
        this.value = value;
    }

    private Valid() {
        this.value = null;
    }

    private <U> MaybeValid<U> ofNullable(U value) {
        return value == null ? empty() : new Valid<>(value);
    }

    @Override
    public T ifInvalid(Function<ValidationFailure, T> unused) {
        return value;
    }

    @Override
    public <U> MaybeValid<U> map(Function<T, U> mapperFunction) {
        Objects.requireNonNull(mapperFunction, "Mapper for valid cannot be null");
        if (!isPresent()) {
            return empty();
        } else {
            return ofNullable(mapperFunction.apply(value));
        }
    }

    @Override
    public <U> MaybeValid<U> flatMap(Function<T, MaybeValid<U>> mapperFunction) {
        Objects.requireNonNull(mapperFunction, "Flat-Mapper for valid cannot be null");
        if (!isPresent()) {
            return empty();
        } else {
            return Objects.requireNonNull(mapperFunction.apply(value));
        }
    }

    @Override
    public MaybeValid<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "Predicate for valid cannot be null");
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    public static <T> MaybeValid<T> empty() {
        return (MaybeValid<T>) EMPTY;
    }

    @Override
    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Valid)) {
            return false;
        }

        Valid<?> other = (Valid<?>) obj;
        return Objects.equals(value, other.value);
    }
}
