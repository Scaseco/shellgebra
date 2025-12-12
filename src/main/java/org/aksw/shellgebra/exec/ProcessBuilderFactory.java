package org.aksw.shellgebra.exec;

@Deprecated // I think it can be deleted, superseded by IProcessBuilder
public interface ProcessBuilderFactory {
    ProcessBuilderBase create(String ...argv);
}
