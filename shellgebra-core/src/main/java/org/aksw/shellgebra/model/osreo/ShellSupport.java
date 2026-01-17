package org.aksw.shellgebra.model.osreo;

public interface ShellSupport
    extends HasCommandOption
{
    ImageIntrospection getOwner();

    String getShellType();
    ShellSupport setShellType(String shellType);

    String getCommandPath();
    ShellSupport setCommandPath(String commandPath);

    /** With this shell in the given container, use this locator command. */
    String getLocatorCommand();
    ShellSupport setLocatorCommand(String locatorCommand);
}
