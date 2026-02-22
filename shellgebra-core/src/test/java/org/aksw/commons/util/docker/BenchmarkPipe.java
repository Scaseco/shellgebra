package org.aksw.commons.util.docker;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.io.pipe.JavaPipe;
import org.aksw.shellgebra.io.pipe.PipeBase;
import org.aksw.shellgebra.io.pipe.PosixPipe;
import org.apache.commons.io.IOUtils;
import org.testcontainers.shaded.org.bouncycastle.util.Arrays;

public class BenchmarkPipe {

    @Test
    public void test1() throws InterruptedException, IOException {
        testCore(JavaPipe.create());
    }

    @Test
    public void test2() throws InterruptedException, IOException {
        testCore(PosixPipe.open());
    }

    public void testCore(PipeBase pipe) throws InterruptedException, IOException {
        Measure m = measureThroughput(pipe, 5000);
        System.out.println(m + " - " + m.throughput());
        pipe.inputStream().close();
        pipe.outputStream().close();
    }

    record Measure(long bytesRead, Duration duration) {
        public BigDecimal throughput() {
            return new BigDecimal(bytesRead / (duration.toMillis() * 0.001f));
        }
    }

    public static Measure measureThroughput(PipeBase pipe, long millis) throws InterruptedException, IOException {
        Stopwatch sw = Stopwatch.createStarted();

        long[] readData = {0};

        Thread readerThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            try {
                while (!Thread.interrupted()) {
                    int n = pipe.inputStream().read(buffer);
                    if (n < 0) {
                        break;
                    }
                    readData[0] += n;
                }
            } catch (InterruptedIOException e) {
                // Ignore
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(pipe.inputStream());
            }
        });
        readerThread.start();

        long i = 0;
        long elapsedMs;
        byte[] b = new byte[4096];
        Arrays.fill(b, (byte)0x0f);
        try {
            while ((elapsedMs = sw.elapsed(TimeUnit.MILLISECONDS)) < millis) {
                // for (long x = 0; x < 10000; ++x) {
                pipe.outputStream().write(b);
            }
        } finally {
            pipe.outputStream().close();
        }
        readerThread.interrupt();
        readerThread.join();

        return new Measure(readData[0], Duration.ofMillis(sw.elapsed(TimeUnit.MILLISECONDS)));
    }
}
