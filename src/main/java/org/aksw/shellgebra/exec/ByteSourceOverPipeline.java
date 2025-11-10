package org.aksw.shellgebra.exec;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

public class ByteSourceOverPipeline
    extends ByteSource
{
    private static final Logger logger = LoggerFactory.getLogger(ByteSourceOverPipeline.class);

    private List<ProcessBuilderBase> processBuilders;
    private List<FileWriterTask> fileWriters;
    private ByteSource inputSource;

    public ByteSourceOverPipeline(List<ProcessBuilderBase> processBuilders, List<FileWriterTask> fileWriters,
            ByteSource inputSource) {
        super();
        this.processBuilders = Objects.requireNonNull(processBuilders);
        this.fileWriters = Objects.requireNonNull(fileWriters);
        this.inputSource = inputSource;

        if (processBuilders.isEmpty()) {
            throw new IllegalArgumentException("List of process builders must not be empty.");
        }
    }

    @Override
    public InputStream openStream() throws IOException {
        List<Process> processes = ProcessBuilderBase.startPipeline(processBuilders);
        Process firstProcess = processes.get(0);
        Process lastProcess = processes.get(processes.size() - 1);

        Thread transferThread = null;

        for (FileWriterTask task : fileWriters) {
            task.start();
        }

        if (inputSource != null) {
            transferThread = new Thread(() -> {
                try (OutputStream dest = firstProcess.getOutputStream()) {
                    try (InputStream src = inputSource.openStream()) {
                        src.transferTo(dest);
                    }
                    dest.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            transferThread.start();
        }

        InputStream in = lastProcess.getInputStream();

        return new FilterInputStream(in) {
            @Override
            public void close() throws IOException {
                super.close();

                for (FileWriterTask task : fileWriters) {
                    try {
                        task.close();
                    } catch (Throwable t) {
                        logger.warn("Error trying to close " + task, t);
                    }
                }

            }
        };
    }
}