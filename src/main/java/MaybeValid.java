/*
 * Copyright 2018 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */


import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Monad interface for valid or invalid
 *
 * @author gakshintala
 * @since 220
 */
public interface MaybeValid<T> {

    T ifInvalid(Function<ValidationFailure, T> errorGenerator);

    <U> MaybeValid<U> map(Function<T, U> mapperFunction);

    <U> MaybeValid<U> flatMap(Function<T, MaybeValid<U>> mapperFunction);

    MaybeValid<T> filter(Predicate<T> predicate);
    
    boolean isPresent();
    
}
