package org.aksw.shellgebra.exec;

import org.aksw.commons.util.docker.Argv;
import org.aksw.commons.util.docker.ImageIntrospector;
import org.aksw.commons.util.docker.ImageIntrospectorCaching;
import org.aksw.commons.util.docker.ImageIntrospectorImpl;
import org.aksw.jenax.model.osreo.ImageIntrospection;
import org.aksw.jenax.model.osreo.ShellSupport;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.vshell.registry.CommandAvailability;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class SysRuntimeFactoryDocker {
    private ImageIntrospector imageIntrospector;

    public SysRuntimeFactoryDocker(ImageIntrospector imageIntrospector) {
        this.imageIntrospector = imageIntrospector;
    }

    public static SysRuntimeFactoryDocker create() {
        Model shellModel = RDFDataMgr.loadModel("shell-ontology.ttl");
        // Could use a default disk-based database to track command availabilities.
        CommandAvailability cmdAvailability = CommandAvailability.get(); // new CommandAvailability();
        ImageIntrospector imageIntrospector = ImageIntrospectorImpl.of(shellModel, cmdAvailability);
        imageIntrospector = new ImageIntrospectorCaching(imageIntrospector);
        return new SysRuntimeFactoryDocker(imageIntrospector);
    }

//    public ProcessBuilderFactoryDocker newProcessBuilder(String imageRef) {
//        ImageIntrospection introspection = imageIntrospector.inspect(imageRef, true);
//        ShellSupport bash = introspection.getShellStatus().get("bash");
//        if (bash == null) {
//            throw new RuntimeException("No bash found");
//        }
//
//        Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.forString().addAllNonNull(bash.getCommandOption()).buildList());
//        String locatorCmd = bash.getLocatorCommand();
//        Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);
//        Argv existsCmd = Argv.of("test", "-e");
//
//        InvokableProcessBuilderHost result = new InvokableProcessBuilderDocker();
//        return result;
//    }

    public SysRuntimeCore createCore(String imageRef) {
        ImageIntrospection introspection = imageIntrospector.inspect(imageRef, true);
        ShellSupport bash = introspection.getShellStatus().get("bash");
        if (bash == null) {
            throw new RuntimeException("No bash found");
        }
//
        Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.forString().addAllNonNull(bash.getCommandOption()).buildList());
        SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv);
        return core;
    }

    public SysRuntimeDocker create(String imageRef) {
        ImageIntrospection introspection = imageIntrospector.inspect(imageRef, true);
        ShellSupport bash = introspection.getShellStatus().get("bash");
        if (bash == null) {
            throw new RuntimeException("No bash found");
        }

        Argv entrypointArgv = Argv.ofArgs(bash.getCommandPath(), ListBuilder.forString().addAllNonNull(bash.getCommandOption()).buildList());
        String locatorCmd = bash.getLocatorCommand();
        Argv locatorArgv = locatorCmd == null ? null : Argv.of(locatorCmd);

        Argv existsCmd = Argv.of("test", "-e");

//        Argv loc = null;
//        String str = bash.getLocatorCommand();
//        if (str != null) {
//            loc = Argv.of(str);
//            List<String> locator = new ArrayList<>(2);
//            locator.add(bash.getLocatorCommand());
//            str = bash.getCommandOption();
//            if (str != null) {
//                locator.add(str);
//            }
//            loc = locator.toArray(String[]::new);
//        }

        SysRuntimeCoreDocker core = ImageIntrospectorImpl.findKeepAlive(imageRef, entrypointArgv);
        SysRuntimeDocker result = new SysRuntimeDocker(core, locatorArgv, existsCmd);
        CommandAvailability cmdAvailability = CommandAvailability.get();
        cmdAvailability.put(core.getEntrypoint().command(), ExecSites.docker(imageRef), true);
        return result;
    }
}
