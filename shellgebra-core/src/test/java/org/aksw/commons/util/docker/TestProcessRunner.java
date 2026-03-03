package org.aksw.commons.util.docker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper;
import org.aksw.shellgebra.exec.graph.ProcessIoWrapper.ExecResult;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.graph.ProcessRunnerPosix;
import org.aksw.shellgebra.processbuilder.ProcessBuilderDockerRun;
import org.aksw.shellgebra.processbuilder.ProcessBuilderGroup;
import org.aksw.shellgebra.processbuilder.ProcessBuilderPipeline;
import org.aksw.shellgebra.registry.init.InitCommandRegistry;
import org.aksw.shellgebra.shim.cmd.GenericCodecArgs;
import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.aksw.vshell.registry.ProcessBuilderNative;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    @Test
    public void testNative() throws Exception {
        String expected = "testNative: OK";
        Process process = ProcessBuilderNative.of("/bin/echo", expected).start();
        ExecResult actual = consume(process);
        assertEquals(expected, actual.out());
    }

    @Test
    public void testJvm() throws Exception {
        String expected = "testJvm: OK";
        JvmCommandRegistry jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());
        Process process = ProcessBuilderJvm.of(jvmCmdRegistry, "/bin/echo", expected).start();
        ExecResult actual = consume(process);
        assertEquals(expected, actual.out());
    }

    @Test
    public void testDocker() throws Exception {
        String expected = "testDocker: OK";
        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        Process process = ProcessBuilderDockerRun.of("nestio/lbzip2", fileMapper, "/bin/echo", expected).start();
        ExecResult actual = consume(process);
        assertEquals(expected, actual.out());
    }

    public static ExecResult consume(Process process) throws Exception {
        ExecResult result = ProcessIoWrapper
            .builder(process)
            .setInputWriterUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 10000; ++i) {
                    out.write("" + i);
                    out.newLine();
                }
                logger.info("Data generation thread terminated.");
            })
            .consume();
        return result;
    }


    @Test
    public void test01() throws Exception {
        JvmCommandRegistry jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());

        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        // System.out.println("Process 6");
        Process process = ProcessBuilderPipeline.of(
            // ProcessBuilderJvm.of("/bin/head", "-n10"),
            ProcessBuilderNative.of("/bin/head", "-n10"),
            ProcessBuilderDockerRun.of("nestio/lbzip2", fileMapper, "/usr/bin/lbzip2", "-c"),
            ProcessBuilderJvm.of(jvmCmdRegistry, "/jvm/bzip2", "-d"))
            // ProcessBuilderJvm.of("/bin/cat"))
            .start();
        System.out.println("Process started");

        ExecResult actual = consume(process);
        System.out.println(actual.out());
    }

    @Test
    // @Disabled
    public void testGroup() throws Exception {
        JvmCommandRegistry jvmCmdRegistry = InitCommandRegistry.initJvmCmdRegistry(new JvmCommandRegistry());

        FileMapper fileMapper = FileMapper.of("/tmp/shared");


        Process process = ProcessBuilderGroup.of(
            ProcessBuilderJvm.of(jvmCmdRegistry, "/bin/echo", "Process 1"),
            ProcessBuilderNative.of("head", "-n 2"),

            ProcessBuilderJvm.of(jvmCmdRegistry, "/bin/echo", "Process 2"),
            ProcessBuilderNative.of("head", "-n 4"),

            ProcessBuilderJvm.of(jvmCmdRegistry, "/bin/echo", "Process 3"),
            ProcessBuilderPipeline.of(
                ProcessBuilderJvm.of(jvmCmdRegistry, "/bin/head", "-n10"),
                ProcessBuilderDockerRun.of("nestio/lbzip2", fileMapper, "/usr/bin/lbzip2", "-c"),
                ProcessBuilderJvm.of(jvmCmdRegistry, "/jvm/bzip2", "-d"))
        ).start();


        ExecResult actual = consume(process);
        System.out.println(actual.out());
//
//        try (ProcessIoWrapper runner = ProcessIoWrapper.of(process)) {
//            runner.setOutputLineReaderUtf8(logger::info);
//            runner.setErrorLineReaderUtf8(logger::info);
//            runner.setInputWriterUtf8(out -> {
//                logger.info("Data generation thread started.");
//                for (int i = 0; i < 10000; ++i) {
//                    out.write("" + i);
//                    out.newLine();
//                }
//                out.flush();
//                logger.info("Data generation thread terminated.");
//            });
//            process.waitFor();
//
//            // InitCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());
//
//        }
    }


    // @Test
//    public void test02() throws Exception {
//        FileMapper fileMapper = FileMapper.of("/tmp/shared");
//
//        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
//            runner.setOutputLineReaderUtf8(logger::info);
//            runner.setErrorLineReaderUtf8(logger::info);
//            runner.setInputPrintStreamUtf8(out -> {
//                logger.info("Data generation thread started.");
//                for (int i = 0; i < 1000; ++i) {
//                    out.println("" + i);
//                }
//                out.flush();
//                logger.info("Data generation thread terminated.");
//            });
//
//            // XXX Low-level ProcessBuilder.start(runner) is perhaps the better direction?
//            // The process builder can take the pipes and e.g. start the docker container.
//            // This is not the responsibility of the runner. Although, could the runner provide default setups for
//            // docker containers?
//            // Also, the purpose of command line parsing is to auto-detect which files need to be
//            // bind mounted.
//            // So the idea is that given parsed command line, the process builder can auto-wire the arguments.
//
//            System.out.println("stdin source: " + SysRuntime.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
//
//            ProcessBuilder pb1 = new ProcessBuilder("head", "-n 2");
//            runner.start(pb1).waitFor();
//            Thread.sleep(1000);
//            // System.out.println("Available: " + runner.internalIn().available() + " on " + ContainerUtils.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
//            if (false) {
//                // Issue: It seems that SOMETIMES (not reliable) data is delivered to the read end of any open
//                // reader.
//                runner.internalIn().transferTo(System.out);
//            }
//            System.out.println("Process 2: Starting.");
//            runner.start(new ProcessBuilder("head", "-n 2")).waitFor();
//            System.out.println("Process 2: Terminated.");
//
//            if (false) {
//                TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());
//
//                // ProcessBuilderJvm jvmProcessBuilder = ProcessBuilderJvm.of("/bin/cat");
//                // Process jvmProcess = jvmProcessBuilder.start(runner);
//                // runner.startJvm(ProcessBuilderJvm.of("/bin/cat")).waitFor();
//
//
//                // runner.start(ProcessBuilderDocker.of("head", "-n 2").entrypoint("bash"));
//                ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
//                    .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                    .waitFor();
//    //            ProcessBuilderDocker.of("head", "-n 2")
//    //                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//    //                .waitFor();
//
//                // System.out.println("exit code: " + jvmProcess.exitValue());
//            }
//        }
//    }

    // @Test
    public void testDocker2() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        Process process = ProcessBuilderDockerRun.of("echo", "DOCKERTESTMSG")
        .imageRef("ubuntu:24.04").fileMapper(fileMapper).start(); // .entrypoint("bash")


        ProcessIoWrapper runner = ProcessIoWrapper.builder(process)
                .setOutputLineReaderUtf8(logger::info)
                .setErrorLineReaderUtf8(logger::info)
                .setInputWriterUtf8(out -> {
                    logger.info("Data generation thread started.");
                    for (int i = 0; i < 1000; ++i) {
                        out.write("" + i);
                        out.newLine();
                    }
                    out.flush();
                    logger.info("Data generation thread terminated.");
                })
                .exec();

        // long pid = ProcessHandle.current().pid()
//        try (FileInputStream in = new FileInputStream(runner.inputPipe().toFile())) {
////             System.out.println("fd is " + ContainerUtils.extractFD(in.getFD()));
//             int fdVal = FileDescriptorCast.using(in.getFD()).as(Integer.class);
//             Path procPath = Paths.get("/proc/self/fd/" + fdVal);
//             System.out.println("fd = " + fdVal);
//             System.out.println("proc path = " + procPath);
//        }

        process.waitFor();

        // SharedSecrets.getJavaIOFileDescriptorAccess();
//            ProcessBuilderDocker.of("head", "-n 2")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                .waitFor();

    }

    @Test
    @Disabled
    public void testProcessBuilderLbzip2() throws Exception {
        String expectedStr = "Hello World";
        Path in = Files.createTempFile("data-", ".bz2");
        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                BZip2CompressorOutputStream bzip = new BZip2CompressorOutputStream(out)) {
                bzip.write(expectedStr.getBytes(StandardCharsets.UTF_8));
                bzip.finish();
                bzip.flush();
                Files.write(in, out.toByteArray());
            }

            // TODO FileMapper could be part of the process runner
            FileMapper fileMapper = FileMapper.of("/tmp/shared");
            try (ProcessRunner runner = ProcessRunnerPosix.create()) {
                String[] args = new String[] {"/usr/bin/lbzip2", "-cd", in.toString()};
                ArgsModular<GenericCodecArgs> model = GenericCodecArgs.parse(args);
                model.toArgList().args();

                ProcessBuilderDockerRun
                    .of("/usr/bin/lbzip2", "-cd", in.toString())
                    .imageRef("nestio/lbzip2")
                    .commandParser(GenericCodecArgs::parse)
                    .fileMapper(fileMapper)
                    .start(runner);

                // Close the process-facing pipes so that any remaining data can be consumed without
                // causing a deadlock while waiting for new data.
                runner.shutdown();

                String actualStr = IOUtils.toString(runner.getInputStream(), StandardCharsets.UTF_8);
                assertEquals(expectedStr, actualStr);
            }
        } finally {
            Files.deleteIfExists(in);
        }
    }
}
