package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.Objects;

import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.transformer.TokenTransformBase;

import com.github.dockerjava.api.model.AccessMode;

public class CmdTokenTransformBindFiles
    extends TokenTransformBase
{
    private FileMapper fileMapper;

    public CmdTokenTransformBindFiles(FileMapper fileMapper) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
    }

    @Override
    public Token transform(TokenPath arg) {
        String hostPath = arg.path();
        String containerPath = fileMapper.allocate(hostPath, AccessMode.ro);
        Token result = hostPath.equals(containerPath)
            ? arg
            : new TokenPath(containerPath);
        return result;
    }
}
