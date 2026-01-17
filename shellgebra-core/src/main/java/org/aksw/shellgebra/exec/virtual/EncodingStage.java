package org.aksw.shellgebra.exec.virtual;

import com.google.common.io.ByteSource;

import org.aksw.shellgebra.exec.stage.BoundStage;
import org.aksw.shellgebra.exec.stage.FileWriterTask;

public class EncodingStage
    implements VirtualStage
{
    protected String encodingName;

	@Override
	public BoundStage from(ByteSource input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoundStage from(FileWriterTask input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoundStage from(BoundStage input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BoundStage fromNull() {
		// TODO Auto-generated method stub
		return null;
	}
}
