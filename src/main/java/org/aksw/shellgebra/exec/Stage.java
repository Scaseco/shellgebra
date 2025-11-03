package org.aksw.shellgebra.exec;

import com.google.common.io.ByteSource;

public interface Stage {
    // TODO Supply input stream - mount via named pipe
    BoundStage from(ByteSource input);
    BoundStage from(FileWriterTask input);
    BoundStage from(BoundStage input);

    // TODO Is there a difference between '0-byte input' and 'no input' on stdin?
    BoundStage fromNull();
}
