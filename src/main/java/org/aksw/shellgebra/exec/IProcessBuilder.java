package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IProcessBuilder<X extends IProcessBuilder<X>> {
    List<String> command();
    X command(String... command);
    X command(List<String> command);

    Path directory();
    X directory(Path directory);

    Map<String, String> environment();

    boolean redirectErrorStream();

    X redirectErrorStream(boolean redirectErrorStream);

    Process build() throws IOException;
}
