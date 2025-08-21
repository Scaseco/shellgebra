package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgPath;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.redirect.Redirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.OpenMode;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;

import com.github.dockerjava.api.model.AccessMode;

/**
 * Extract file name arguments and map them as flat files under a given prefix.
 *
 * <pre>
 * CmdOpExec(/usr/bin/cat CmdOpPath(/etc/lsb_release))
 * </pre>
 *
 * becomes
 *
 * <pre>
 * CmdOpExec(/usr/bin/cat CmdOpPath(/home/user/my.file))
 * Binds={/home/user/my.file:/shared/my.file:ro}
 * </pre>
 */
public class CmdOpTransformBindFiles
    extends CmdOpTransformBase
{
    private FileMapper fileMapper;

//    public CmdOpTransformBindFiles(String containerSharedPath) {
//        this(new FileMapper(containerSharedPath));
//    }

    public CmdOpTransformBindFiles(FileMapper fileMapper) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
    }

    public FileMapper getFileMapper() {
        return fileMapper;
    }

    @Override
    public CmdOp transform(CmdOpExec op, List<CmdArg> args) {
        List<CmdArg> newArgs = new ArrayList<>(args.size());
        for (CmdArg arg : args) {
            if (arg instanceof CmdArgPath pathArg) {
                String hostPath = pathArg.path();
                String containerPath = fileMapper.allocate(hostPath, AccessMode.ro);
                newArgs.add(new CmdArgPath(containerPath));
            } else if (arg instanceof CmdArgRedirect r) {
                Redirect redirect = r.redirect();
                if (redirect instanceof RedirectFile f) {
                    Redirect newRedirect = processRedirect(f);
                    newArgs.add(new CmdArgRedirect(newRedirect));
                } else {
                    newArgs.add(arg);
                }
            } else {
                newArgs.add(arg);
            }
        }

        List<Redirect> newRedirects = new ArrayList<>(op.redirects().size());
        for (Redirect r : op.redirects()) {
            if (r instanceof RedirectFile f) {
                Redirect x = processRedirect(f);
                newRedirects.add(x);
            } else {
                newRedirects.add(r);
            }
        }

        return new CmdOpExec(op.getName(), newArgs, newRedirects);
    }

    protected Redirect processRedirect(RedirectFile f) {
        String hostPath = f.file();
        OpenMode openMode = f.openMode();
        AccessMode accessMode = toAccessMode(openMode);
        String containerPath = fileMapper.allocate(hostPath, accessMode);
        Redirect newRedirect = new RedirectFile(containerPath, openMode, f.fd());
        return newRedirect;
    }

    public static AccessMode toAccessMode(OpenMode openMode) {
        Set<OpenMode> writeModes = Set.of(OpenMode.WRITE_TRUNCATE, OpenMode.WRITE_APPEND, OpenMode.CLOBBER);
        AccessMode result = writeModes.contains(openMode)
            ? AccessMode.rw
            : AccessMode.ro;
        return result;
    }
}
