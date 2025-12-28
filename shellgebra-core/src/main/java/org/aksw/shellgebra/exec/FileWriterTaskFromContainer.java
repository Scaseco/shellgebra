package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.command.WaitContainerResultCallback;

public class FileWriterTaskFromContainer
    extends FileWriterTaskBase
{
    private Logger logger = LoggerFactory.getLogger(FileWriterTaskFromContainer.class);

    private GenericContainer<?> container;
    private List<FileWriterTask> subTasks;

    public FileWriterTaskFromContainer(GenericContainer<?> container, Path path, PathLifeCycle pathLifeCycle, List<FileWriterTask> subTasks) {
        super(path, pathLifeCycle);
        this.container = container;
        this.subTasks = subTasks;
    }

    @Override
    protected void prepareWriteFile() throws IOException {
    }

    @Override
    protected void runWriteFile() throws IOException {
        pathLifeCycle.beforeExec(outputPath);

        for (FileWriterTask subTask : subTasks) {
            subTask.start();
        }

        container.start();
    }

    @Override
    public void start() {
        try {
            runWriteFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            container.close();
        } finally {

            for (FileWriterTask subTask : subTasks) {
                try {
                    subTask.close();
                } catch (Exception e) {
                    logger.warn("Failure while closing sub task.", e);
                }
            }

            pathLifeCycle.afterExec(outputPath);
        }
    }

    @Override
    protected void onCompletion() throws IOException {
    }

    @Override
    public void waitForCompletion() throws ExecutionException, InterruptedException {
        container.getDockerClient()
            .waitContainerCmd(container.getContainerId())
            .exec(new WaitContainerResultCallback())
            .awaitCompletion();
    }

    @Override
    public void abort() {
        container.close();
    }
}
