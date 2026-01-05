package org.aksw.shellgebra.model.osreo;

import org.apache.jena.rdf.model.Resource;

public interface ShellSupport
    extends HasCommandOption
{
    ImageIntrospection getOwner();

    Resource getShellType();
    ShellSupport setShellType(Resource shellType);

    String getCommandPath();
    ShellSupport setCommandPath(String commandPath);

    /** With this shell in the given container, use this locator command. */
    String getLocatorCommand();
    ShellSupport setLocatorCommand(String locatorCommand);
}
