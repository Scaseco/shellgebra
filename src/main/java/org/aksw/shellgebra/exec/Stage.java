package org.aksw.shellgebra.exec;

import com.google.common.io.ByteSource;

public interface Stage {
    // TODO Supply input stream - mount via named pipe

    /**
     * Use a byte source for input.
     * Note: If the byte source wraps a live input stream, then it is the caller's responsibility to close it.
     */
    BoundStage from(ByteSource input);

    BoundStage from(FileWriterTask input);

    BoundStage from(BoundStage input);

    // TODO Is there a difference between '0-byte input' and 'absent input' on stdin?
    BoundStage fromNull();
}
