package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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

}
