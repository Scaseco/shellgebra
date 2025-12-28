package org.aksw.vshell.shim.rdfconvert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import picocli.CommandLine;

public class ArgsParserPicocli<T>
    implements ArgsParser<T>
{
    private final Supplier<T> modelSupplier;

    public ArgsParserPicocli(Supplier<T> modelSupplier) {
        super();
        this.modelSupplier = modelSupplier;
    }

    @Override
    public T parse(String[] args) {
        T model = modelSupplier.get();
        CommandLine cmd = new CommandLine(model);
        cmd.parseArgs(args);
        return model;
    }

    public static <T> ArgsParser<T> of(Class<T> clazz) {
        return new ArgsParserPicocli<>(asInstanceSupplier(clazz));
    }

    public static <T> ArgsParser<T> of(Supplier<T> instanceCreator) {
        return new ArgsParserPicocli<>(instanceCreator);
    }

    private static <T> Supplier<T> asInstanceSupplier(Class<T> clazz) {
        Constructor<T> ctor;
        try {
            ctor = clazz.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        return asInstanceSupplier(ctor);
    }

    private static <T> Supplier<T> asInstanceSupplier(Constructor<T> ctor) {
        return () -> newInstance(ctor);
    }

    private static <T> T newInstance(Constructor<T> ctor) {
        try {
            return ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
