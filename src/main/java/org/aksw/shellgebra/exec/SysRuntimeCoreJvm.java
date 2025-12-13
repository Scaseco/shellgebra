package org.aksw.shellgebra.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;

import org.aksw.commons.util.docker.Argv;
import org.aksw.vshell.registry.DynamicInputFromStream;
import org.aksw.vshell.registry.DynamicOutputFromStream;
import org.aksw.vshell.registry.JvmCommand;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.JvmExecCxt;

public class SysRuntimeCoreJvm
    implements SysRuntimeCore
{
    private JvmCommandRegistry jvmCmdRegistry;

    public SysRuntimeCoreJvm(JvmCommandRegistry jvmCmdRegistry) {
        super();
        this.jvmCmdRegistry = jvmCmdRegistry;
    }

    @Override
    public IProcessBuilder<?> newProcessBuilder() {
        // return new ProcessBuilderJv
        throw new UnsupportedOperationException();
    }

    @Override
    public String execCmd(String... argv) throws IOException, InterruptedException {
        Argv a = Argv.of(argv);
        String c = a.command();
        String[] s = a.newArgs();
        JvmCommand cmd = jvmCmdRegistry.require(c);

        // TODO Tidy up with property try-with-resources.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        JvmExecCxt cxt = new JvmExecCxt(
            null, Map.of(), Path.of(""),
            DynamicInputFromStream.of(InputStream.nullInputStream()),
            DynamicOutputFromStream.of(out),
            DynamicOutputFromStream.of(err));
        cmd.run(cxt, a);

        byte[] outBytes = out.toByteArray();
        String result = new String(outBytes, StandardCharsets.UTF_8);

        // Stage stage = cmd.newStage(s);
        // String result = stage.fromNull().toByteSource().asCharSource(StandardCharsets.UTF_8).read();
        return result;
    }

    @Override
    public int runCmd(String... argv) throws IOException, InterruptedException {
        int result;
        try {
            String tmp = execCmd(argv);
            result = 0;
        } catch (NoSuchElementException e) {
            result = 127;
        }
        return result;
    }

    @Override
    public void close() {
    }
}
