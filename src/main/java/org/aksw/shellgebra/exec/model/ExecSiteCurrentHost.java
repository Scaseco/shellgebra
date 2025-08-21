package org.aksw.shellgebra.exec.model;

public record ExecSiteCurrentHost()
    implements ExecSite
{
    private static ExecSiteCurrentHost INSTANCE;

    public static ExecSiteCurrentHost get() {
        if (INSTANCE == null) {
            synchronized (ExecSiteCurrentHost.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExecSiteCurrentHost();
                }
            }
        }
        return INSTANCE;
    }
}
