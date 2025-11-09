package org.aksw.shellgebra.exec;

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

    public static ListBuilder<String> forString(int initialCapacity) {
        return new ListBuilder<>(new ArrayList<>(initialCapacity), String[]::new);
    }

    public static ListBuilder<String> forString() {
        return new ListBuilder<>(new ArrayList<>(), String[]::new);
    }
}
