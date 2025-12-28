package org.aksw.shellgebra.registry.tool;

public class ShellInfo {
    protected String shellLocation;
    protected String shellName;

    protected ShellInfo(String shellLocation) {
        super();
        this.shellLocation = shellLocation;
    }

    public void setShellName(String shellName) {
        this.shellName = shellName;
    }

    public String getShellName() {
        return shellName;
    }

}
