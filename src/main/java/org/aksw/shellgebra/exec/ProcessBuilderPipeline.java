package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;

import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.PathResource;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public class ProcessBuilderPipeline
    extends ProcessBuilderBase<ProcessBuilderPipeline>
{
    private List<IProcessBuilder<?>> processBuilders;

    public ProcessBuilderPipeline() {
        super();
        this.processBuilders = List.of();
    }

    public ProcessBuilderPipeline processBuilders(IProcessBuilder<?>... processBuilders) {
        processBuilders(List.of(processBuilders));
        return self();
    }

    public ProcessBuilderPipeline processBuilders(List<IProcessBuilder<?>> processBuilders) {
        this.processBuilders = List.copyOf(processBuilders);
        return self();
    }

    public static ProcessBuilderPipeline of(IProcessBuilder<?> ... processBuilders) {
        return new ProcessBuilderPipeline().processBuilders(processBuilders);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        int n = processBuilders.size();
        if (n == 0) {
            throw new IllegalStateException("Pipeline must have at least one member.");
        }

        PathResource priorPath = null;

        List<Process> processes = new ArrayList<>(n);
        List<PathResource> pipes = new ArrayList<>(n - 1);
        PathLifeCycle lifeCycle = PathLifeCycles.deleteAfterExec(PathLifeCycles.namedPipe());

        for (int i = 0; i < n; ++i) {
            boolean isLast = i == n - 1;
            IProcessBuilder<?> current = processBuilders.get(i);
            IProcessBuilder<?> tmp = current.clone();

            // Set up redirect input.
            if (priorPath == null) {
                tmp.redirectInput(new JRedirectJava(Redirect.from(executor.inputPipe().toFile())));
            } else {
                tmp.redirectInput(new JRedirectJava(Redirect.from(priorPath.getPath().toFile())));
            }

            // Set up redirect output.
            if (isLast) {
                tmp.redirectOutput(new JRedirectJava(Redirect.to(executor.outputPipe().toFile())));
            } else {
                PathResource thisPath = new PathResource(SysRuntime.newNamedPipePath(), lifeCycle);
                thisPath.open();
                tmp.redirectOutput(new JRedirectJava(Redirect.to(thisPath.getPath().toFile())));
                pipes.add(thisPath);
                priorPath = thisPath;
            }

            // Set up redirect error.
            tmp.redirectError(new JRedirectJava(Redirect.to(executor.errorPipe().toFile())));

            // Start.
            Process process = tmp.start(executor);
            processes.add(process);
        }

        return new ProcessPipeline(processes, pipes);
    }

    @Override
    protected ProcessBuilderPipeline cloneActual() {
        ProcessBuilderPipeline result = new ProcessBuilderPipeline();
        return result;
    }
}
