package org.aksw.vshell.shim.rdfconvert;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public record ArgsModular<T>(T model, ArgumentListRenderer<? super T> renderer, Predicate<? super T> stdinTest)
    implements Args
{
    public ArgsModular {
        Objects.requireNonNull(model);
        Objects.requireNonNull(renderer);
    }

    @Override
    public ArgumentList toArgList() {
        ArgumentList result = renderer.toArgumentList(model);
        return result;
    }

    public static <T> Args of(T model, ArgumentListRenderer<? super T> renderer, Predicate<? super T> stdinTest) {
        return new ArgsModular<>(model, renderer, stdinTest);
    }

    /**
     * Boolean for whether the command is known to (not) read from stdin.
     * An absent value means unknown.
     * Engines are free to interpret an absent value as they see fit,
     * log warnings or fail when encountering unknown values.
     */
    @Override
    public Optional<Boolean> readsStdin() {
        Boolean verdict = stdinTest == null ? null : stdinTest.test(model);
        return Optional.ofNullable(verdict);
    }
}
