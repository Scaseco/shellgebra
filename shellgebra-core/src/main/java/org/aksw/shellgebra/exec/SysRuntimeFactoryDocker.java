package org.aksw.shellgebra.exec;

import java.util.List;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.aksw.commons.util.docker.Argv;
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.shellgebra.introspect.ShellCatalogEntry;
import org.aksw.shellgebra.introspect.ShellProbeResult;
import org.aksw.shellgebra.model.osreo.ImageIntrospector;
import org.aksw.vshell.registry.ExecSiteProbeResults;

public class SysRuntimeFactoryDocker {
    private ImageIntrospector imageIntrospector;
    private Table<String, Argv, Boolean> imageToEntrypoints;
    private ExecSiteProbeResults probeResults;
    private Table<String, String, ShellProbeResult> imageToShellToProbeResult = HashBasedTable.create();

    private static SysRuntimeFactoryDocker instance = null;

    public static SysRuntimeFactoryDocker get() {
        if (instance == null) {
            synchronized (SysRuntimeFactoryDocker.class) {
                if (instance == null) {
                    instance = create();
                }
            }
        }
        return instance;
    }

    private static SysRuntimeFactoryDocker create() {
        // Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        // Could use a default disk-based database to track command availabilities.
        ExecSiteProbeResults probeResults = ExecSiteProbeResults.get(); // new CommandAvailability();
        Table<String, Argv, Boolean> imageToEntrypoints = HashBasedTable.create();
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(); // (shellModel, probeResults);
        // imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);
        return new SysRuntimeFactoryDocker(imageIntrospector, imageToEntrypoints, probeResults);
    }

    public SysRuntimeFactoryDocker(ImageIntrospector imageIntrospector, Table<String, Argv, Boolean> imageToEntrypoints, ExecSiteProbeResults probeResults) {
        this.imageIntrospector = imageIntrospector;
        this.imageToEntrypoints = imageToEntrypoints;
        this.probeResults = probeResults;
    }

    public ExecSiteProbeResults getProbeResults() {
        return probeResults;
    }

    public SysRuntimeCoreDocker createCore(String imageRef) {
        return createCore(imageRef, true);
    }

    public SysRuntimeCoreDocker createCore(String imageRef, boolean pullIfAbsent) {
//        List<ShellCatalogEntry> shellCatalog = ImageIntrospectorImpl.getShellCatalog();
//        List<ShellCatalogEntry> bashCatalog = ImageIntrospectorImpl.getShellSubCatalog(shellCatalog, "bash");
        // SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, pullIfAbsent, bashCatalog, imageToEntrypoints, probeResults);
        List<Argv> keepAliveCmdCatalog = ImageIntrospectorImpl.getKeepAliveCatalog();
        SysRuntimeCoreDocker core = ImageIntrospectorImpl.startKeptAlive(imageRef, keepAliveCmdCatalog, probeResults);
        return core;
    }

    // Issue: Find keep alive *may* already searches for a shell.
    // In principle, the keep alive command does not require a shell.
    // So the createCore may already cache shell probe results.
    public SysRuntimeDocker create(String imageRef) {
        List<ShellCatalogEntry> shellCatalog = ImageIntrospectorImpl.getShellCatalog();
        List<ShellCatalogEntry> bashCatalog = ImageIntrospectorImpl.getShellSubCatalog(shellCatalog, "bash");
        ShellCatalogEntry shellEntry = bashCatalog.get(0);
        SysRuntimeCoreDocker core = createCore(imageRef, true);

        ShellProbeResult shellProbeResult = imageIntrospector.findShell(core, shellEntry);
        Argv entrypointArgv = Argv.ofArgs(shellProbeResult.location(), ListBuilder.ofString().addAllNonNull(shellProbeResult.commandOption()).buildList());
        String locatorCmd = shellProbeResult.locatorCommand();
        Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);
        Argv existsCmd = Argv.of("test", "-e");
        SysRuntimeDocker result = new SysRuntimeDocker(core, locatorArgv, existsCmd);
        return result;
    }

    public ShellProbeResult findShell(String imageRef, boolean pullIfAbsent, String shellName) {
        ShellProbeResult result = imageToShellToProbeResult.get(imageRef, shellName);
        if (result == null) {
            List<ShellCatalogEntry> shellCatalog = ImageIntrospectorImpl.getShellCatalog();
            List<ShellCatalogEntry> bashCatalog = ImageIntrospectorImpl.getShellSubCatalog(shellCatalog, shellName);
            // Check whether we already have cached info for that shell.
            // If not, then launch a container using a keep alive command and then search for the shell.
            try (SysRuntimeCoreDocker runtime = createCore(imageRef, pullIfAbsent)) {
                for (ShellCatalogEntry shellEntry : bashCatalog) {
                    ShellProbeResult probeResult = imageIntrospector.findShell(runtime, shellEntry);
                    if (probeResult != null) {
                        result = probeResult;
                        imageToShellToProbeResult.put(imageRef, shellName, result);
                        break;
                    }
                }
            }
//            ShellProbeResult shellProbeResult = imageIntrospector.findShell(core, shellEntry);
//            Argv entrypointArgv = Argv.ofArgs(shellProbeResult.location(), ListBuilder.ofString().addAllNonNull(shellProbeResult.commandOption()).buildList());
//            String locatorCmd = shellProbeResult.locatorCommand();
//            Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);
//            Argv existsCmd = Argv.of("test", "-e");
//            SysRuntimeDocker result = new SysRuntimeDocker(core, locatorArgv, existsCmd);
        }
        return result;
    }


//  public ProcessBuilderFactoryDocker newProcessBuilder(String imageRef) {
//  ImageIntrospection introspection = imageIntrospector.inspect(imageRef, true);
//  ShellSupport bash = introspection.getShellStatus().get("bash");
//  if (bash == null) {
//      throw new RuntimeException("No bash found");
//  }
//
//  Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.forString().addAllNonNull(bash.getCommandOption()).buildList());
//  String locatorCmd = bash.getLocatorCommand();
//  Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);
//  Argv existsCmd = Argv.of("test", "-e");
//
//  InvokableProcessBuilderHost result = new InvokableProcessBuilderDocker();
//  return result;
//}

//  // ImageIntrospectorImpl
//  //imageIntrospector.c
//
//
//  ImageIntrospectorImpl.findKeepAlive(imageRef, null, probeResults);
//  // entrypoint - catalog
//  ImageIntrospectorImpl.startKeptAlive(imageRef, null, null, probeResults);
//
//  List<Argv> entrypoints = ImageIntrospectorImpl.getKeepAliveCatalog();
//  ImageIntrospectorImpl.startKeptAlive(imageRef, null, entrypoints, probeResults);
//  //ImageIntrospectorImpl.
//
//  SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv, probeResults);
//
//  imageIntrospector.findShell(null, imageRef, null)

//  ImageIntrospection introspection = imageIntrospector.findShell(imageRef, true);
//  ShellSupport bash = introspection.getShellStatus().get("bash");
//  if (bash == null) {
//      throw new RuntimeException("No bash found");
//  }
////
//  Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.ofString().addAllNonNull(bash.getCommandOption()).buildList());
//  SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv, probeResults);

// SysRuntimeCoreDocker core = imageIntrospector.findShell(runtime, entry);
//  ShellSupport bash = introspection.getShellStatus().get("bash");
//  if (bash == null) {
//      throw new RuntimeException("No bash found");
//  }

    //  Argv loc = null;
    //  String str = bash.getLocatorCommand();
    //  if (str != null) {
    //      loc = Argv.of(str);
    //      List<String> locator = new ArrayList<>(2);
    //      locator.add(bash.getLocatorCommand());
    //      str = bash.getCommandOption();
    //      if (str != null) {
    //          locator.add(str);
    //      }
    //      loc = locator.toArray(String[]::new);
    //  }

        // SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv, probeResults);

//    public SysRuntimeDocker create(String imageRef) {
//        ImageIntrospection introspection = imageIntrospector.findShell(imageRef, true);
//        ShellSupport bash = introspection.getShellStatus().get("bash");
//        if (bash == null) {
//            throw new RuntimeException("No bash found");
//        }
//
//        Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.ofString().addAllNonNull(bash.getCommandOption()).buildList());
//        String locatorCmd = bash.getLocatorCommand();
//        Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);
//
//        Argv existsCmd = Argv.of("test", "-e");
//
////        Argv loc = null;
////        String str = bash.getLocatorCommand();
////        if (str != null) {
////            loc = Argv.of(str);
////            List<String> locator = new ArrayList<>(2);
////            locator.add(bash.getLocatorCommand());
////            str = bash.getCommandOption();
////            if (str != null) {
////                locator.add(str);
////            }
////            loc = locator.toArray(String[]::new);
////        }
//
//        SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv, probeResults);
//        SysRuntimeDocker result = new SysRuntimeDocker(core, locatorArgv, existsCmd);
//
//        return result;
//    }
}
