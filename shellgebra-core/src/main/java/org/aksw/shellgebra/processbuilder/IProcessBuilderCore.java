package org.aksw.shellgebra.processbuilder;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;

/**
 * Process builder for common settings without the 'command' property.
 * Redirect configuration is fundamental because it is needed to form pipelines.
 *
 * Redirects are realized using pipes.
 * This interface exposes information about which types of pipes are supported
 * with the current configuration. Supported pipe types are 'anonymous' and 'named'.
 */
public interface IProcessBuilderCore<X extends IProcessBuilderCore<X>>
    extends Cloneable
{
    /** Returns an independent copy of this process builder. */
    X clone();

    Path directory();
    X directory(Path directory);

    Map<String, String> environment();

    boolean redirectErrorStream();
    X redirectErrorStream(boolean redirectErrorStream);

    Process start(ProcessRunner executor) throws IOException;

    default Process start() throws IOException {
        ProcessRunner cxt = ProcessRunnerPosix.create();
        Process result = start(cxt);
        cxt.releaseInternalIo();
        return result;
    }

    X redirectInput(JRedirect redirect);
    JRedirect redirectInput();
    default X redirectInput(Redirect redirect) {
        return redirectInput(new JRedirectJava(redirect));
    }

    X redirectOutput(JRedirect redirect);
    JRedirect redirectOutput();
    default X redirectOutput(Redirect redirect) {
        return redirectOutput(new JRedirectJava(redirect));
    }

    X redirectError(JRedirect redirect);
    JRedirect redirectError();
    default X redirectError(Redirect redirect) {
        return redirectError(new JRedirectJava(redirect));
    }

    /**
     * Whether the process builder can read from anonymous pipes.
     *
     * Docker containers can only bind-mount named pipes but not anonymous pipes.
     * When building pipelines, this flag is used to avoid needless
     * intermediate anon pipes where named ones can be used directly.
     *
     * For pipelines, this is the value of the first process builder.
     */
    boolean supportsAnonPipeRead();

    /**
     * Whether the process builder can write to an anonymous pipe.
     *
     * Docker containers can only bind-mount named pipes but not anonymous pipes.
     * When building pipelines, this flag is used to avoid needless
     * intermediate anon pipes where named ones can be used directly.
     *
     * For pipelines, this is the value of the last process builder.
     */
    boolean supportsAnonPipeWrite();


    /**
     * Whether a named pipe can be used with the process builder without risking blocking
     * due to multiple connections being made to it.
     * This method must only return true if only a single connection to that pipe will be openend.
     *
     * A process builder for host or docker may return true.
     * But a group with two or more 'true-returning' members will return false.
     */
    boolean supportsDirectNamedPipe();

    /**
     * Whether the configured command will read from stdin.
     * Used to avoid generation of needless named or anon pipes such as in "echo foo | echo bar":
     * where the second link does not read the data from the prior link.
     *
     */
    boolean accessesStdIn();
}
