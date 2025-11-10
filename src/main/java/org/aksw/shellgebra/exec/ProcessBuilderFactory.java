package org.aksw.shellgebra.exec;

public interface ProcessBuilderFactory {
    ProcessBuilderBase create(String ...argv);
}
