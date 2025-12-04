package org.aksw.commons.util.docker;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.ProcessBuilderDocker;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.vshell.registry.ProcessBuilderJvm;
import org.newsclub.net.unix.FileDescriptorCast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcessRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    @Test
    public void test01() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        try (ProcessRunner runner = ProcessRunner.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 10000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

            ProcessBuilder pb1 = new ProcessBuilder("head", "-n 2");
            runner.start(pb1).waitFor();
            Thread.sleep(1000);
            // System.out.println("Available: " + runner.internalIn().available() + " on " + ContainerUtils.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
            if (false) {
                // Issue: It seems that SOMETIMES (not reliable) data is delivered to the read end of any open
                // reader.
                runner.internalIn().transferTo(System.out);
            }
            System.out.println("Process 2: Starting.");
            runner.start(new ProcessBuilder("head", "-n 4")).waitFor();
            System.out.println("Process 2: Terminated.");

            TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());

            // ProcessBuilderJvm jvmProcessBuilder = ProcessBuilderJvm.of("/bin/cat");
            // Process jvmProcess = jvmProcessBuilder.start(runner);
            runner.startJvm(ProcessBuilderJvm.of("/bin/head", "-n10")).waitFor();


                // runner.start(ProcessBuilderDocker.of("head", "-n 2").entrypoint("bash"));
//                ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
//                    .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                    .waitFor();
    //            ProcessBuilderDocker.of("head", "-n 2")
    //                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
    //                .waitFor();

                // System.out.println("exit code: " + jvmProcess.exitValue());
        }
    }

    @Test
    public void test02() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        try (ProcessRunner runner = ProcessRunner.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 1000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

            // XXX Low-level ProcessBuilder.start(runner) is perhaps the better direction?
            // The process builder can take the pipes and e.g. start the docker container.
            // This is not the responsibility of the runner. Although, could the runner provide default setups for
            // docker containers?
            // Also, the purpose of command line parsing is to auto-detect which files need to be
            // bind mounted.
            // So the idea is that given parsed command line, the process builder can auto-wire the arguments.

            System.out.println("stdin source: " + ContainerUtils.getFdPath(((FileInputStream)runner.internalIn()).getFD()));

            ProcessBuilder pb1 = new ProcessBuilder("head", "-n 2");
            runner.start(pb1).waitFor();
            Thread.sleep(1000);
            // System.out.println("Available: " + runner.internalIn().available() + " on " + ContainerUtils.getFdPath(((FileInputStream)runner.internalIn()).getFD()));
            if (false) {
                // Issue: It seems that SOMETIMES (not reliable) data is delivered to the read end of any open
                // reader.
                runner.internalIn().transferTo(System.out);
            }
            System.out.println("Process 2: Starting.");
            runner.start(new ProcessBuilder("head", "-n 2")).waitFor();
            System.out.println("Process 2: Terminated.");

            if (false) {
                TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());

                // ProcessBuilderJvm jvmProcessBuilder = ProcessBuilderJvm.of("/bin/cat");
                // Process jvmProcess = jvmProcessBuilder.start(runner);
                // runner.startJvm(ProcessBuilderJvm.of("/bin/cat")).waitFor();


                // runner.start(ProcessBuilderDocker.of("head", "-n 2").entrypoint("bash"));
                ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
                    .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
                    .waitFor();
    //            ProcessBuilderDocker.of("head", "-n 2")
    //                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
    //                .waitFor();

                // System.out.println("exit code: " + jvmProcess.exitValue());
            }
        }
    }

    // @Test
    public void testDocker() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");

        try (ProcessRunner runner = ProcessRunner.create()) {
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);
            runner.setInputPrintStreamUtf8(out -> {
                logger.info("Data generation thread started.");
                for (int i = 0; i < 1000; ++i) {
                    out.println("" + i);
                }
                out.flush();
                logger.info("Data generation thread terminated.");
            });

        // long pid = ProcessHandle.current().pid()
        try (FileInputStream in = new FileInputStream(runner.inputPipe().toFile())) {
//             System.out.println("fd is " + ContainerUtils.extractFD(in.getFD()));
             int fdVal = FileDescriptorCast.using(in.getFD()).as(Integer.class);
             Path procPath = Paths.get("/proc/self/fd/" + fdVal);
             System.out.println("fd = " + fdVal);
             System.out.println("proc path = " + procPath);
        }


        // SharedSecrets.getJavaIOFileDescriptorAccess();

        ProcessBuilderDocker.of("echo", "DOCKERTESTMSG")
            .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
            .waitFor();
//            ProcessBuilderDocker.of("head", "-n 2")
//                .imageRef("ubuntu:24.04").entrypoint("bash").fileMapper(fileMapper).start(runner)
//                .waitFor();

        }
    }

}
