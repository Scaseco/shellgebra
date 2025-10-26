package org.aksw.shellgebra.exec.model;

public record ExecSiteCurrentJvm()
    implements ExecSite
{
    private static ExecSiteCurrentJvm INSTANCE;

    public static ExecSiteCurrentJvm get() {
        if (INSTANCE == null) {
            synchronized (ExecSiteCurrentJvm.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExecSiteCurrentJvm();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public <T> T accept(ExecSiteVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
