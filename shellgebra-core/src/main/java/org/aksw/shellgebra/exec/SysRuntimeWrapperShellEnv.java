package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.aksw.shellgebra.exec.shell.ShellEnv;
import org.apache.commons.exec.ExecuteException;

public class SysRuntimeWrapperShellEnv
    extends SysRuntimeWrapperBase<SysRuntime>
{
    private ShellEnv shellEnv;

    public SysRuntimeWrapperShellEnv(SysRuntime delegate, ShellEnv shellEnv) {
        super(delegate);
        this.shellEnv = Objects.requireNonNull(shellEnv);
    }

    // Pattern that rejects names with unescaped slashes.
    private static final Pattern localNamePattern = Pattern.compile("^(?:(?:[^/]|//)*)$");
    protected boolean isLocalName(String name) {
        return localNamePattern.matcher(name).matches();
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("^(?:(?:[^/]|//)*)$");
        String[] tests = { "abc", "a//b", "////", "/", "a/b", "///" };

        for (String s : tests) {
            System.out.printf("%-6s → %s%n", s, p.matcher(s).matches());
        }
    }

    protected String whichOrNull(String cmdName) throws IOException, InterruptedException {
        String result;
        try {
            result = super.which(cmdName);
        } catch (ExecuteException e) {
            if (e.getExitValue() != 1) {
                throw new ExecuteException(e.getMessage(), e.getExitValue(), e);
            }
            result = null;
        }
        return result;
    }

    @Override
    public String which(String cmdName) throws IOException, InterruptedException {
        String result = whichOrNull(cmdName);
        if (result == null) {
            if (isLocalName(cmdName)) {
                List<String> pathStrs = shellEnv.streamPathCandidates(cmdName).toList();
                for (String pathStr : pathStrs) {
                    Path path = Path.of(pathStr).toAbsolutePath();
                    if (Files.exists(path)) {
                        result = path.toString();
                        break;
                    }
                }
            }
        }
        if (result == null) {
            // Feign a process execution exception.
            // XXX Should abstract into something like a FileNotFound exception.
            throw new ExecuteException("Process exited with non-zero code: 1", 1);
        }
        return result;
    }
}
