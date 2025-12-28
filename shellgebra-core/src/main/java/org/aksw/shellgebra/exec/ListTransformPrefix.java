package org.aksw.shellgebra.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Transform that creates a copy of a given list with a prefix items prepended.
 * @param <T>
 */
public class ListTransformPrefix<T>
    implements Function<List<T>, List<T>>
{
    private final List<T> prefix;

    public ListTransformPrefix(List<T> prefix) {
        super();
        this.prefix = List.copyOf(prefix);
    }

    @Override
    public List<T> apply(List<T> t) {
        List<T> result = new ArrayList<>(prefix.size() + t.size());
        result.addAll(prefix);
        result.addAll(t);
        return Collections.unmodifiableList(result);
    }
}
