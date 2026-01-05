package org.aksw.shellgebra.model.osreo;

public interface Shell
    extends HasLabel, HasProbeLocation, HasCommandOption
{
    /**
     * Some shells have a built-in command locator.
     * For example, bash has built-in support for "which".
     */
    String getLocatorCommand();
    Shell setLocatorCommand(String locatorCommand);
}
