package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.aksw.shellgebra.exec.graph.JRedirect;
import org.aksw.shellgebra.exec.graph.ProcessRunner;

public interface IProcessBuilder<X extends IProcessBuilder<X>>
    extends Cloneable
{
    X clone();

    List<String> command();
    X command(String... command);
    X command(List<String> command);

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
