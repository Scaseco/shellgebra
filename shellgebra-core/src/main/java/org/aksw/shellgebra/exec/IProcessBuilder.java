package org.aksw.shellgebra.exec;

import java.util.List;

// XXX Perhaps the String[] command should be moved to a sub-interface
public interface IProcessBuilder<X extends IProcessBuilder<X>>
    extends IProcessBuilderCore<X>
{
    List<String> command();
    X command(String... command);
    X command(List<String> command);
}
