package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

/** Process builder for common settings without the command property. */
public interface IProcessBuilderCore<X extends IProcessBuilderCore<X>>
    extends Cloneable
{
    X clone();

    Path directory();
    X directory(Path directory);

    Map<String, String> environment();

    boolean redirectErrorStream();
    X redirectErrorStream(boolean redirectErrorStream);

    Process start(ProcessRunner executor) throws IOException;

    X redirectInput(JRedirect redirect);
    JRedirect redirectInput();

    X redirectOutput(JRedirect redirect);
    JRedirect redirectOutput();

    X redirectError(JRedirect redirect);
    JRedirect redirectError();

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
