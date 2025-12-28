package org.aksw.commons.util.docker;

import java.util.Objects;
import java.util.concurrent.CompletionException;

sealed interface Result<T> {

    T getOrElseThrow();

    record Ok<T>(T value) implements Result<T> {
        @Override public T getOrElseThrow() { return value; }
    }

    record Err<T>(Throwable error) implements Result<T> {
        public Err {
            error = Objects.requireNonNull(error);
        }
        @Override public T getOrElseThrow() { throw new CompletionException(error); }
    }
}
