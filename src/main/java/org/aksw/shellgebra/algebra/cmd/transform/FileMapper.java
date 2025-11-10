package org.aksw.shellgebra.algebra.cmd.transform;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.prefix.ShortNameMgr;
import org.apache.commons.io.FileUtils;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;

public class FileMapper {
    private List<Bind> binds;

    // Base-path in the container under which to bind-mount files from the host.
    private String containerSharedPath;
    // private Path hostTempPath;
    private ShortNameMgr shortNameMgr;

    // XXX Also copy shortNameMgr?
    @Override
    public FileMapper clone() {
        return new FileMapper(this.containerSharedPath, this.shortNameMgr.clone(), new ArrayList<>(this.binds));
    }

    public static FileMapper of(String containerSharedPath) {
        return new FileMapper(containerSharedPath);
    }

    protected FileMapper(String containerSharedPath) {
        this(containerSharedPath, new ShortNameMgr());
    }

    public FileMapper(String containerSharedPath, ShortNameMgr shortNameMgr) {
        this(containerSharedPath, shortNameMgr, new ArrayList<>());
    }

    public FileMapper(String containerSharedPath, ShortNameMgr shortNameMgr, List<Bind> binds) {
        super();
        this.containerSharedPath = Objects.requireNonNull(containerSharedPath);
        this.shortNameMgr = Objects.requireNonNull(shortNameMgr);

        // Remove trailing backslashes
        this.containerSharedPath = this.containerSharedPath.replaceAll("/+$", "");
        this.binds = binds;
    }

    public List<Bind> getBinds() {
        return binds;
    }

    public String getContainerPath(String hostPath) {
        String result = getBinds().stream().filter(b -> b.getPath().equals(hostPath))
            .map(Bind::getVolume).map(Volume::getPath).findFirst().orElse(null);
        return result;
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

    public static String allocateTempFilename(String prefix, String suffix) {
        String name = Stream.of(prefix, Long.toString(System.nanoTime()), suffix)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.joining("-"));

        return name;
    }

    public static Path allocateTempPath(String prefix, String suffix) {
        String name = allocateTempFilename(prefix, suffix);
        Path tmpDir = FileUtils.getTempDirectory().toPath();
        Path tmpFile = tmpDir.resolve(name);
        return tmpFile;
    }

    public Entry<Path, String> allocateTempFile(String prefix, String suffix, AccessMode accessMode) {
        Path tmpFile = allocateTempPath(prefix, suffix);
        String tmpFileStr = tmpFile.toAbsolutePath().toString();

        String containerPath = allocate(tmpFileStr, accessMode);
        return Map.entry(tmpFile, containerPath);
    }
}
