package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.processbuilder.ProcessBuilderBase;

@Deprecated // I think it can be deleted, superseded by IProcessBuilder
public interface ProcessBuilderFactory {
    ProcessBuilderBase create(String ...argv);
}
