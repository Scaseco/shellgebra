package org.aksw.shellgebra.exec;

import java.util.Objects;

import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.vshell.registry.ExecSiteResolver;
import org.aksw.vshell.registry.JvmCommandRegistry;

public class SysRuntimeExecSiteFactoryImpl
    implements SysRuntimeExecSiteFactory
{
    @Override
    public SysRuntimeFactory getFactory(ExecSite execSite) {
        return null;
    }


    public static class ExecSiteVisitorSysRuntimeFactory
        implements ExecSiteVisitor<SysRuntime>
    {
        private JvmCommandRegistry jvmCmdRegistry;

        public ExecSiteVisitorSysRuntimeFactory(JvmCommandRegistry jvmCmdRegistry) {
            super();
            this.jvmCmdRegistry = Objects.requireNonNull(jvmCmdRegistry);
        }

        @Override
        public SysRuntime visit(ExecSiteDockerImage execSite) {
            String imageRef = execSite.imageRef();
            SysRuntime result = SysRuntimeFactoryDocker.create().create(imageRef);
            return result;
        }

        @Override
        public SysRuntime visit(ExecSiteCurrentHost execSite) {
            return SysRuntimeImpl.forCurrentOs();
        }

        @Override
        public SysRuntime visit(ExecSiteCurrentJvm execSite) {
            ExecSiteResolver resolver;
        }
    }
}
