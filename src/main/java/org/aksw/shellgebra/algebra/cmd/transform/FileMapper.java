package org.aksw.shellgebra.algebra.cmd.transform;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.docker.ContainerPathResolver;
import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.apache.commons.io.FileUtils;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;

public class FileMapper {
    private List<Bind> binds = new ArrayList<>();

    // Base-path in the container under which to bind-mount files from the host.
    private String containerSharedPath;

    // private Path hostTempPath;

    private ShortNameMgr shortNameMgr;

    private ContainerPathResolver containerPathResolver;

    public static FileMapper of(String containerSharedPath) {
        return new FileMapper(containerSharedPath);
    }

    protected FileMapper(String containerSharedPath) {
        this(containerSharedPath, new ShortNameMgr());
    }

    public FileMapper(String containerSharedPath, ShortNameMgr shortNameMgr) {
        super();
        this.containerSharedPath = Objects.requireNonNull(containerSharedPath);
        this.shortNameMgr = Objects.requireNonNull(shortNameMgr);

        // Remove trailing backslashes
        this.containerSharedPath = this.containerSharedPath.replaceAll("/+$", "");
        this.containerPathResolver = ContainerPathResolver.create();
    }

    public List<Bind> getBinds() {
        return binds;
    }

//    public String allocateReadOnly(String hostPath) {
//        return allocate(hostPath, AccessMode.ro);
//    }

    /** Return a path in the container under which the host path is exposed. */
    public String allocate(String hostPath, AccessMode accessMode) {
        String containerLocalName = shortNameMgr.allocate(hostPath).localName();
        String containerFullPath = containerSharedPath + "/" + containerLocalName;

        Bind bind = new Bind(hostPath, new Volume(containerFullPath), accessMode);
        binds.add(bind);
        return containerFullPath;
    }

    public Entry<Path, String> allocateTempFile(String prefix, String suffix, AccessMode accessMode) {
        String name = Stream.of(prefix, Long.toString(System.nanoTime()), suffix)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.joining("-"));

        Path tmpDir = FileUtils.getTempDirectory().toPath();
        Path tmpFile = tmpDir.resolve(name);
        String tmpFileStr = tmpFile.toAbsolutePath().toString();

        String containerPath = allocate(tmpFileStr, accessMode);
        return Map.entry(tmpFile, containerPath);
    }
}
