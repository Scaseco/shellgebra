package org.aksw.jenax.engine.qlever;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedPipe {
    private static final Logger logger = LoggerFactory.getLogger(NamedPipe.class);

    // From sys/stat.h
    private static final int S_IFMT  = 0170000; // bitmask for file type
    private static final int S_IFIFO = 0010000; // FIFO (named pipe)

    /** True for both FIFOs and anonymous pipes (Linux pipes). */
    public static boolean isFifoType(Path path, boolean followSymlinks) throws IOException {
        LinkOption[] opts = followSymlinks ? new LinkOption[0]
                : new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
        Map<String, Object> attrs = Files.readAttributes(path, "unix:mode", opts);
        int mode = (Integer) attrs.get("mode");
      return (mode & S_IFMT) == S_IFIFO;
    }

    public static boolean isNamedPipe(Path path, boolean followSymlinks) throws IOException {
        boolean result = isFifoType(path, followSymlinks) && !PosixPipe.isAnonymousProcPipe(path);

        // ChatGPT generated additional check:
//        Map<String, Object> attrs =
//                Files.readAttributes(p, "unix:mode,unix:nlink"); // follows symlinks by default
//
//        int mode = (int) attrs.get("mode");
//        if ((mode & S_IFMT) != S_IFIFO) return false;
//
//        int nlink = (int) attrs.get("nlink");
//        return nlink >= 1; // directory entry exists => named FIFO

        return result;
    }

    public static void create(Path path) throws IOException {
        String absPathStr = path.toAbsolutePath().toString();
        String resolvedCmd = SystemUtils.which("mkfifo");
        SystemUtils.runAndWait(logger::info, resolvedCmd, absPathStr);
    }

    public static Path newNamedPipePath() throws IOException {
        String baseDir = System.getProperty("java.io.tmpdir");
        String fileName = "named-pipe-" + System.nanoTime();
        Path result = Path.of(baseDir).resolve(fileName);
        return result;
    }

    public static Path create() throws IOException {
        Path result = newNamedPipePath();
        create(result);
        return result;
    }
}
