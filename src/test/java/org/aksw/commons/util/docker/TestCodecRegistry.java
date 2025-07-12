package org.aksw.commons.util.docker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.registry.codec.JavaCodec;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestCodecRegistry {

    @Test
    public void testJavaBzip2() throws IOException {
        assertRoundTrip("bzip2", "test content");
    }

    @Test
    public void testJavaGzip() throws IOException {
        assertRoundTrip("gz", "test content");
    }

    private static void assertRoundTrip(String codecName, String expected) throws IOException {
        String actual;

        JavaCodec codec = CodecRegistry.get().requireJavaCodec(codecName);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (OutputStream out = codec.encoder().outputStreamTransform().apply(baos)) {
                out.write(expected.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }

            try (InputStream in = codec.decoder().inputStreamTransform().apply(new ByteArrayInputStream(baos.toByteArray()))) {
                actual = IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        }

        Assert.assertEquals(expected, actual);
    }
}
