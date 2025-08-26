package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.exec.ExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

/** Writer task based on a system process created from a system call. */
public class FileWriterTaskFromProcessBuilder extends FileWriterTaskViaExecutor {
    private static final Logger logger = LoggerFactory.getLogger(FileWriterTaskFromProcessBuilder.class);

    private List<ProcessBuilder> processBuilders;
    private List<Process> processes;

    private List<FileWriterTask> inputTasks;
    private ByteSource inputSource;

    public FileWriterTaskFromProcessBuilder(Path outputPath, PathLifeCycle pathLifeCycle, List<ProcessBuilder> processBuilders, List<FileWriterTask> inputTasks, ByteSource inputSource) {
        super(outputPath, pathLifeCycle);
        this.processBuilders = processBuilders;
        this.inputTasks = inputTasks;
        this.inputSource = inputSource;
    }

    protected final void beforeExec() throws IOException {
        pathLifeCycle.beforeExec(outputPath);
    }

    @Override
    protected void prepareWriteFile() throws IOException {
    }

    @Override
    public void runWriteFile() throws ExecuteException, IOException {
        for (FileWriterTask inputTask : inputTasks) {
            inputTask.start();
        }

        processes = ProcessBuilder.startPipeline(processBuilders);

        if (inputSource != null) {
            Process firstProcess = processes.get(0);
            try (OutputStream out = firstProcess.getOutputStream()) {
                try (InputStream in = inputSource.openStream()) {
                    in.transferTo(out);
                }
                out.flush();
            }
        }

    }

    @Override
    public void abortActual() {
        for (FileWriterTask inputTask : inputTasks) {
            inputTask.abort();
        }

        for (Process process : processes) {
            process.destroy();
        }
    }

    @Override
    protected void onCompletion() throws IOException {
        // nothing to do
    }

    @Override
    public String toString() {
        return super.toString(); // + " " + cmdLine;
    }
}
