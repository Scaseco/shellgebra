package org.aksw.shellgebra.exec;

import com.google.common.io.ByteSource;

public interface ExecFactory {
    // TODO Supply input stream - mount via named pipe
    ExecBuilder forInput(ByteSource input);
    ExecBuilder forInput(FileWriterTask input);
    ExecBuilder forInput(ExecBuilder input);
    ExecBuilder forNullInput();
}
