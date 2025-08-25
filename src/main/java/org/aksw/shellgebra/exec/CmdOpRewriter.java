package org.aksw.shellgebra.exec;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpTransformBindFiles;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;

/**
 *
 */
public class CmdOpRewriter
{
    /**
     * Rewrite all file references in the cmd op such that they refer to paths in the container.
     * Register all binds with the fileMapper.
     */
    // XXX Should files be declared as read/write in the expression? Or should this be handles on
    // the tool registry level?
    public static CmdOp rewriteForContainer(CmdOp cmdOp, FileMapper fileMapper) {
        // If we run in a container, we need to know a location that is mounted on the host so that it can be
        // shared with other containers.
        // FileMapper fileMapper = FileMapper.of("/shared");
        CmdOpTransformBindFiles bindTransform = new CmdOpTransformBindFiles(fileMapper);
        CmdOp containerizedCmd = CmdOpTransformer.transform(cmdOp, bindTransform);
        return containerizedCmd;
    }
}

