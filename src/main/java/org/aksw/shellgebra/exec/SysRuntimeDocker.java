package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class SysRuntimeDocker
    implements SysRuntime
{
    private String dockerImageName;



    @Override
    public String which(String cmdName) throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String quoteFileArgument(String fileName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmdString compileString(CmdOp op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] compileCommand(CmdOp op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CmdStrOps getStrOps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createNamedPipe(Path path) throws IOException {
        // TODO Auto-generated method stub

    }

    public GenericContainer<?> startKeptAlive(DockerImageName image) {
        String[][] candidates = new String[][]{
            // GNU coreutils (PATH lookup)
            {"sleep", "infinity"},
            // BusyBox/Alpine (PATH lookup)
            {"sleep", "365d"},
            // Absolute paths (cover usrmerge + busybox)
            {"/bin/sleep", "infinity"},
            {"/usr/bin/sleep", "infinity"},
            {"/bin/sleep", "365d"},
            {"/usr/bin/sleep", "365d"},
            // Tail fallback (very common)
            {"tail", "-f", "/dev/null"},
            {"/bin/tail", "-f", "/dev/null"},
            {"/usr/bin/tail", "-f", "/dev/null"},
            // If BusyBox is present as a single binary:
            {"/bin/busybox", "sleep", "365d"},
            {"/usr/bin/busybox", "sleep", "365d"},
            // Last-resort shell loop IF a shell exists
            {"sh", "-c", "while :; do sleep 1h; done"},
            {"/bin/sh", "-c", "while :; do sleep 1h; done"},
            {"/usr/bin/sh", "-c", "while :; do sleep 1h; done"}
        };

        Exception last = null;
        for (String[] cmd : candidates) {
            try {
                GenericContainer<?> c = new GenericContainer<>(image).withCommand(cmd);
                c.start();

                // Execute a command inside the container
                Container.ExecResult result = c.execInContainer("ls", "-la", "/");

                System.out.println("Exit code: " + result.getExitCode());
                System.out.println("STDOUT:\n" + result.getStdout());
                System.out.println("STDERR:\n" + result.getStderr());

                return c;
            } catch (Exception e) {
                last = e; // try next
            }
        }
        throw new IllegalStateException(
            "Could not find a portable keep-alive command (image may be distroless/scratch).",
            last
        );
    }
}
