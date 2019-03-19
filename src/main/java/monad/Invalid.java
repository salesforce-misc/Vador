package monad;/*
 * Copyright 2018 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */


import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Monad to hold invalid type
 *
 * @author gakshintala
 * @since 220
 */
public class Invalid<T> implements MaybeValid<T> {

    private final ValidationFailure validationFailure;
    private static final MaybeValid<?> EMPTY = new Invalid<>();

    public Invalid(ValidationFailure validationFailure) {
        this.validationFailure = validationFailure;
    }

    private Invalid() {
        this.validationFailure = null;
    }

    @Override
    public T ifInvalid(Function<ValidationFailure, T> errorGenerator) {
        Objects.requireNonNull(errorGenerator, "Error Generator for invalid cannot be null");
        return errorGenerator.apply(validationFailure);
    }

    @Override
    public <U> MaybeValid<U> map(Function<T, U> unused) {
        return new Invalid<>(validationFailure);
    }

    @Override
    public <U> MaybeValid<U> flatMap(Function<T, MaybeValid<U>> unused) {
        return new Invalid<>(validationFailure);
    }

    @Override
    public MaybeValid<T> filter(Predicate<T> unused) {
        return empty();
    }

    public static <T> MaybeValid<T> empty() {
        return (MaybeValid<T>) EMPTY;
    }

    @Override
    public boolean isPresent() {
        return validationFailure != null;
    }

    public ValidationFailure get() {
        if (validationFailure == null) {
            throw new NoSuchElementException("No value present");
        }
        return validationFailure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(validationFailure);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Invalid)) {
            return false;
        }

        Invalid<?> other = (Invalid<?>) obj;
        return Objects.equals(validationFailure, other.validationFailure);
    }
}
