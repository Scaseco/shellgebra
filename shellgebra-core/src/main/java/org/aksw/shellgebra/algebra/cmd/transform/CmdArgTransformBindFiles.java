package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.Objects;
import java.util.Set;

import com.github.dockerjava.api.model.AccessMode;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect.OpenMode;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdArgTransformBase;

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
public class CmdArgTransformBindFiles
    implements CmdArgTransformBase
{
    private FileMapper fileMapper;

    public CmdArgTransformBindFiles(FileMapper fileMapper) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
    }

    @Override
    public CmdArg transform(CmdArgRedirect arg) {
        CmdRedirect inRedirect = arg.redirect();
        CmdRedirect outRedirect = processRedirect(inRedirect);
        CmdArg result = inRedirect.equals(outRedirect)
            ? arg
            : new CmdArgRedirect(outRedirect);
        return result;
    }

    protected CmdRedirect processRedirect(CmdRedirect redirect) {
        RedirectTarget target = redirect.target();
        CmdRedirect result = target instanceof RedirectTargetFile f
            ? processRedirect(redirect, f)
            : redirect;
        return result;
    }

    protected CmdRedirect processRedirect(CmdRedirect redirect, RedirectTargetFile f) {
        String hostPath = f.file();
        OpenMode openMode = redirect.openMode();
        AccessMode accessMode = toAccessMode(openMode);
        String containerPath = fileMapper.allocate(hostPath, accessMode);
        CmdRedirect newRedirect = new CmdRedirect(redirect.fd(), openMode, new RedirectTargetFile(containerPath));
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
