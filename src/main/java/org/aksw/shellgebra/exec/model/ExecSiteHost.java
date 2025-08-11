package org.aksw.shellgebra.exec.model;

public class ExecSiteHost
    implements ExecSite
{
    private static ExecSiteHost INSTANCE;

    public static ExecSiteHost get() {
        if (INSTANCE == null) {
            synchronized (ExecSiteHost.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExecSiteHost();
                }
            }
        }
        return INSTANCE;
    }

    private ExecSiteHost() {
        super();
    }

    @Override
    public String toString() {
        return "ExecSiteHost";
    }
}
