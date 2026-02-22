package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.github.dockerjava.api.command.WaitContainerResultCallback;

import org.aksw.commons.util.docker.ContainerUtils;
import org.aksw.commons.util.docker.HostNameUtils;
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.introspect.ShellCatalogEntry;
import org.aksw.shellgebra.introspect.ShellProbeResult;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.testcontainers.containers.GenericContainer;

public class MainPlaygroundShellgebra {
    public static void main(String[] args) throws IOException, InterruptedException {
//       String imageName = "ubuntu:latest";
//       String imageName = "nestio/lbzip2";
        String imageName = "adfreiburg/qlever:commit-a307781";

        SysRuntimeFactoryDocker factory = SysRuntimeFactoryDocker.get();
        ImageIntrospector introspector = ImageIntrospectorImpl.of();
        try (SysRuntimeCoreDocker runtimeCore = factory.createCore(imageName)) {
            List<ShellCatalogEntry> entries = ImageIntrospectorImpl.getShellSubCatalog(ImageIntrospectorImpl.getShellCatalog(), "bash");
            for (ShellCatalogEntry entry : entries) {
                ShellProbeResult probeResult = introspector.findShell(runtimeCore, entry);
            }

        }


        if (true) {
            return;
        }

//        List<Shell> shells = OsreoUtils.listShells(model);
//        for (Shell shell : shells) {
//            System.out.println(shell);
//        }
//
//        List<LocatorCommand> locatorCommands = OsreoUtils.listLocatorCommands(model);
//        for (LocatorCommand locatorCommand : locatorCommands) {
//            System.out.println(locatorCommand);
//        }
//
//

        System.out.println("Hostname: " + HostNameUtils.getHostName());


        // Define paths
        Path inputFile = Path.of("/home/raven/Datasets/dcat-maven-demo/countries/countries.ttl.bz2").toAbsolutePath();
        Path fifoPath = Path.of("/tmp/output.pipe").toAbsolutePath();

        // Ensure input exists
        if (!Files.exists(inputFile)) {
            throw new IOException("Input file does not exist: " + inputFile);
        }

        // Create named pipe if not exists
        if (!Files.exists(fifoPath)) {
            SysRuntimeImpl.forCurrentOs().createNamedPipe(fifoPath);
//            Process mkfifo = new ProcessBuilder("mkfifo", fifoPath.toString()).start();
//            if (mkfifo.waitFor() != 0) {
//                throw new IOException("Failed to create named pipe: " + fifoPath);
//            }
        }

        String lbzip2Path = ContainerUtils.checkImageForCommand(imageName, "lbzip2");
        System.out.println("Path: " + lbzip2Path);
        String command = lbzip2Path != null ?
                lbzip2Path + " -d -c /mnt/data/input.bz2 > /mnt/fifo/output.pipe" :
                "apt update && apt install -y lbzip2 && " +
                        "lbzip2 -d -c /mnt/data/input.bz2 > /mnt/fifo/output.pipe";

        try (GenericContainer<?> container = new GenericContainer<>(imageName)
                .withFileSystemBind(inputFile.toString(), "/mnt/data/input.bz2")
                .withFileSystemBind(fifoPath.toString(), "/mnt/fifo/output.pipe")
                .withLogConsumer(frame -> System.out.println(frame.getUtf8StringWithoutLineEnding()))
                .withCommand("sh", "-c", command)) {
                // .waitingFor(Wait.forSuccessfulExit())) {

            // ContainerUtils.addCurrentUserAndGroup(container);

            // container.waitingFor(WaitStrategy)
            container.start();
            container.getDockerClient()
                .waitContainerCmd(container.getContainerId())
                .exec(new WaitContainerResultCallback())
                .awaitCompletion();
            System.out.println("Decompression to named pipe completed.");
        }
    }
}
