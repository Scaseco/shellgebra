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

    // redirectError()
}
