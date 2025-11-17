package org.aksw.shellgebra.algebra.cmd.transform;

import java.util.Objects;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectFile.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdArgTransformBase;

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
public class CmdArgTransformBindFiles
    extends CmdArgTransformBase
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
        CmdRedirect result = redirect instanceof RedirectFile f
            ? processRedirect(f)
            : redirect;
        return result;
    }

    protected CmdRedirect processRedirect(RedirectFile f) {
        String hostPath = f.file();
        OpenMode openMode = f.openMode();
        AccessMode accessMode = toAccessMode(openMode);
        String containerPath = fileMapper.allocate(hostPath, accessMode);
        CmdRedirect newRedirect = new RedirectFile(containerPath, openMode, f.fd());
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
