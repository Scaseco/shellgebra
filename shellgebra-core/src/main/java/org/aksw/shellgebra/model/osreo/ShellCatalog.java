package org.aksw.shellgebra.model.osreo;

import java.util.List;

public interface ShellCatalog {
    List<Shell> listShells();
    List<LocatorCommand> listLocatorCommands();
}
