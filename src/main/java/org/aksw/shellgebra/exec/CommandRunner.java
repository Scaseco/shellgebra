package org.aksw.shellgebra.exec;

public interface CommandRunner<T> {
    T call(String... argv);
}
