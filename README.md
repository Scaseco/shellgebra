# Shellgebra
ProcessBuilder on steroids: build shell-like pipelines in Java, then automatically resolve missing commands via containers or JVM implementations — keeping the  JVM out of the hot data path if possible.

The main featuers of this project are:
* Processes can be executed on different execution sites: host, jvm (java-native) and docker.
* Redirects of stdin, stdout and stderr are set up automatically in such ways that they work across execution site boundaries.
* File system abstraction: Write commands as if they were executed on the host system. If such a command gets scheduled to a docker container, then a combination of a command registry and a file mapper are used to transparently rewrite them: File name arguments are identified, bind mounts are set up and the original command is rewritten to make use of in-container paths.

## Roadmap

* Better "ExecSite to Config" handling for docker-based containers. Currently the system internally employs auto-detection of the commands for the entrypoint, keep-alive and locator (`which`). A new API will make this configurable on a per-image basis.

## Overview 

The system comprises the following levels of abstraction:

* ProcessBuilder level: Core primitives to start processes on different execution sites.
* Expression level: An AST representation of process invocations (much like a shell script). An planner then decides on which execution site to run the command.

## Process Builder Level

### Feature Status

* Pipes
- [x] Support for Posix Pipes / Anonmyous Pipes
- [x] Support for Named Pipes
- [ ] Support for Java-native PipedInputStream/PipedOutputStream fallback (pending)

* Shell features
- [ ] Job Control; Sending processes to the background such as with `my-command &`.

### ProcessBuilder Example

The purpose of the ProcessBuilder abstraction is to wire up processes 

The atomic process builders are: 
* `ProcessBuilderJvm`: A process builder over a registry of java implementations of commands.
* `ProcessBuilderNative`: A process builder that executes commands natively on the current operating system. This is similar to java's `java.lang.ProcessBuilder`.
* `ProcessBuilderDocker`: A process builder that executes commands in a docker container. It relies on a registry of command parsers in order to parse out file arguments and map them to paths in the container.

Compoind process builders are:
* `ProcessBuilderPipeline`: Builder for process pipelines, such as `echo foo | cat`. The output of each process in a pipeline step becomes the input of its successor.
* `ProcessBuilderGroup`: Builder to process a sequence of member process builders. The group's input is forwarded to all members that are interested in it.

```java
    @Test
    public void test01() throws Exception {
        FileMapper fileMapper = FileMapper.of("/tmp/shared");
        try (ProcessRunner runner = ProcessRunnerPosix.create()) {
            // Send stdout and stderr to the logger.
            runner.setOutputLineReaderUtf8(logger::info);
            runner.setErrorLineReaderUtf8(logger::info);

            // Set an input data generator. A sequence of integers (one per line).
            runner.setInputPrintStreamUtf8(out -> {
                for (int i = 0; i < 10000; ++i) {
                    out.println("" + i);
                }
                out.flush();
            });

            // Register commands
            TestCommandRegistry.initJvmCmdRegistry(runner.getJvmCmdRegistry());
          
            // Set up a pipeline that runs
            // - Takes 10 lines using the system's `head` command
            // - Uses the `nestio/lbzip2` docker container to run lbzip2
            // - Uses Apache Commons Compress (Java) to decompress the stream
            ProcessBuilderPipeline.of(
                ProcessBuilderNative.of("/bin/head", "-n10"),
                ProcessBuilderDocker.of("/usr/bin/lbzip2", "-c")
                    .interactive(true) // The 'interactive' flag forwards data on stdin to the container.
                                       // If not set, the flag value will be derived from the command metadata.
                    .imageRef("nestio/lbzip2").fileMapper(fileMapper),
                ProcessBuilderJvm.of("/jvm/bzip2", "-d"))
                .start(runner)
                .waitFor();

            // Expected output: The first n lines sent to the logger
            // INFO  TestProcessRunner: 0
            // ...
            // INFO  TestProcessRunner: 9
        }
    }
```

## Expression Level
TDB

