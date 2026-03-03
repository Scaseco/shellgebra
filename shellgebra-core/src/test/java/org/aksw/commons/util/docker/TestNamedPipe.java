package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.io.pipe.NamedPipe;
import org.aksw.shellgebra.io.pipe.PosixPipe;

public class TestNamedPipe {
    @Test
    public void test01() throws IOException {
        Path path = NamedPipe.create();
        try {
            boolean isNamedPipe = NamedPipe.isNamedPipe(path, true);
            assertTrue(isNamedPipe);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testPosixPipeIsNotANamedPipe() throws IOException {
        try (PosixPipe posixPipe = PosixPipe.open()) {
            boolean isNamedPipeRead = NamedPipe.isNamedPipe(posixPipe.getReadEndProcPath(), true);
            assertFalse(isNamedPipeRead);

            boolean isNamedPipeWrite = NamedPipe.isNamedPipe(posixPipe.getReadEndProcPath(), true);
            assertFalse(isNamedPipeWrite);
        }
    }

    /**
     * A named pipe blocks until both of its ends are connected.
     * This means that a second thread is needed to connect both ends of the pipe
     * without causing a dead lock.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testNamedPipeBlock() throws IOException, InterruptedException {
        Path path = NamedPipe.create();
        Thread thread = new Thread(() -> {
            try (OutputStream out = Files.newOutputStream(path)) {
                // nothing to do.
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        thread.start();
        try (InputStream in = Files.newInputStream(path)) {
            // nothing to do.
        }
        thread.join();

        // This should never work:
        // try (OutputStream out = Files.newOutputStream(path)) {
        //     try (InputStream in = Files.newInputStream(path)) {
        //     }
        // }
    }

}
