package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.exec.graph.ProcessBuilderFactory;

public class ProcessBuilderFactoryDocker
    implements ProcessBuilderFactory<ProcessBuilderDocker>
{
    private ProcessBuilderDocker prototype;

    public ProcessBuilderFactoryDocker(ProcessBuilderDocker prototype) {
        super();
        this.prototype = prototype;
    }

    @Override
    public ProcessBuilderDocker get() {
        return prototype.clone();
    }
}
