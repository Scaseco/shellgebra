package org.aksw.commons.util.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import org.aksw.shellgebra.pipe.NamedPipe;
import org.aksw.shellgebra.pipe.PosixPipe;

import junit.framework.Assert;

public class TestNamedPipe {
    @Test
    public void test01() throws IOException {
        Path path = NamedPipe.create();
        try {
            boolean isNamedPipe = NamedPipe.isNamedPipe(path, true);
            Assert.assertTrue(isNamedPipe);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testPosixPipeIsNotANamedPipe() throws IOException {
        try (PosixPipe posixPipe = PosixPipe.open()) {
            boolean isNamedPipeRead = NamedPipe.isNamedPipe(posixPipe.getReadEndProcPath(), true);
            Assert.assertFalse(isNamedPipeRead);

            boolean isNamedPipeWrite = NamedPipe.isNamedPipe(posixPipe.getReadEndProcPath(), true);
            Assert.assertFalse(isNamedPipeWrite);
        }
    }

}
