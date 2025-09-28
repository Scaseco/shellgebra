package org.aksw.vshell.shim.rdfconvert;

import java.util.Objects;

public record ArgsModular<T>(T model, ArgumentListRenderer<? super T> renderer)
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

    public static <T> Args of(T model, ArgumentListRenderer<? super T> renderer) {
        return new ArgsModular<>(model, renderer);
    }
}
