package org.aksw.shellgebra.exec;

import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;

import com.github.dockerjava.api.model.AccessMode;
import com.google.common.io.ByteSource;

public class StageDocker
    implements Stage
{
    // A Docker image reference consists of several components that describe where the image is stored and its identity. These components are:
    // https://docs.docker.com/reference/cli/docker/image/tag/ - [HOST[:PORT]/]NAMESPACE/REPOSITORY[:TAG]
    protected String imageRef;
    protected CmdOp cmdOp;
    protected FileMapper fileMapper;
    protected ContainerPathResolver containerPathResolver;
    protected Function<CmdOpVar, Stage> varResolver;

    public StageDocker(String imageRef, CmdOp cmdOp, FileMapper fileMapper, ContainerPathResolver containerPathResolver, Function<CmdOpVar, Stage> varResolver) {
        super();
        this.imageRef = imageRef;
        this.cmdOp = cmdOp;
        this.fileMapper = fileMapper;
        this.containerPathResolver = containerPathResolver;
        this.varResolver = varResolver;
    }

    @Override
    public BoundStage from(ByteSource input) {
        // Allocate a tmp path
        // String allocate(String hostPath, AccessMode accessMode) {

        // TODO Must create the file writer on demand!
        Entry<Path, String> map = fileMapper.allocateTempFile("byteSource", "", AccessMode.ro);

        Path hostPath = map.getKey();

        // Set up a bind for the input
        FileWriterTask inputTask = new FileWriterTaskFromByteSource(hostPath, PathLifeCycles.namedPipe(), input);
        BoundStage result = from(inputTask);
        return result;
    }

    @Override
    public BoundStage from(FileWriterTask inputTask) {
        return new BoundStageDocker(imageRef, cmdOp, fileMapper, containerPathResolver, inputTask, null, varResolver);
    }

    @Override
    public BoundStage from(BoundStage input) {
        return new BoundStageDocker(imageRef, cmdOp, fileMapper, containerPathResolver, null, input, varResolver);
    }

    @Override
    public BoundStage fromNull() {
        return new BoundStageDocker(imageRef, cmdOp, fileMapper, containerPathResolver, (FileWriterTask)null, null, varResolver);
    }
}
