package org.aksw.shellgebra.exec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

public class ListBuilder<T> {
    private List<T> list = new ArrayList<>();
    private IntFunction<T[]> arrayConstructor;

    private ListBuilder(List<T> list, IntFunction<T[]> arrayConstructor) {
        super();
        this.list = list;
        this.arrayConstructor = arrayConstructor;
    }

    public ListBuilder<T> add(T item) {
        list.add(item);
        return this;
    }

    public ListBuilder<T> addAll(Collection<T> items) {
        list.addAll(items);
        return this;
    }

    public ListBuilder<T> addAll(T... items) {
        list.addAll(Arrays.asList(items));
        return this;
    }

    public ListBuilder<T> addAllNonNull(T... items) {
        for (T item : items) {
            if (item != null) {
                list.add(item);
            }
        }
        return this;
    }

    public List<T> buildList() {
        return List.copyOf(list);
    }

    public T[] buildArray() {
        T[] result = list.toArray(arrayConstructor);
        return result;
    }

    public static ListBuilder<String> ofString(int initialCapacity) {
        return new ListBuilder<>(new ArrayList<>(initialCapacity), String[]::new);
    }

    public static ListBuilder<String> ofString() {
        return new ListBuilder<>(new ArrayList<>(), String[]::new);
    }

    /**
     * Convenience method to use ListBuilder.forObject(MyType.class);
     * Alterantive to ListBuilder.of(String[]::new).
     */
    @SuppressWarnings("unchecked")
    public static <T> ListBuilder<T> of(Class<T> clz) {
        // Only do the reflection overhead when an array is requested.
        IntFunction<T[]> fn = i -> {
            try {
                Constructor<T[]> arrayCtor = (Constructor<T[]>)clz.arrayType().getConstructor(Integer.TYPE);
                return arrayCtor.newInstance(i);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        };
        ListBuilder<T> result = of(fn);
        return result;
    }

    public static <T> ListBuilder<T> of(IntFunction<T[]> fn) {
        return new ListBuilder<>(new ArrayList<>(), fn);
    }
}
