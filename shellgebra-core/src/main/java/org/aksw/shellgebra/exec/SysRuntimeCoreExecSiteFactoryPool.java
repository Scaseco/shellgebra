package org.aksw.shellgebra.exec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.vshell.registry.JvmCommandRegistry;
import org.apache.commons.io.IOUtils;

/**
 * A pool for launching runtimes for exec sites on demand.
 * The runtimes are kept running until the pool is shutdown.
 */
public class SysRuntimeCoreExecSiteFactoryPool
    implements SysRuntimeCoreExecSiteFactory
{
    private JvmCommandRegistry jvmCmdRegistry;
    private SysRuntimeFactoryDocker sysRuntimeDockerFactory;
    private Map<ExecSite, SysRuntimeCore> execSiteToRuntime = new ConcurrentHashMap<>();

    protected SysRuntimeCoreExecSiteFactoryPool(JvmCommandRegistry jvmCmdRegistry, SysRuntimeFactoryDocker sysRuntimeDockerFactory) {
        super();
        this.jvmCmdRegistry = jvmCmdRegistry;
        this.sysRuntimeDockerFactory = sysRuntimeDockerFactory;
    }

    public static SysRuntimeCoreExecSiteFactory of(JvmCommandRegistry jvmCmdRegistry) {
        return of(jvmCmdRegistry, SysRuntimeFactoryDocker.get());
    }

    public static SysRuntimeCoreExecSiteFactory of(JvmCommandRegistry jvmCmdRegistry, SysRuntimeFactoryDocker sysRuntimeDockerFactory) {
        return new SysRuntimeCoreExecSiteFactoryPool(jvmCmdRegistry, sysRuntimeDockerFactory);
    }

    // Instances should be closed after used.
    @Override
    public SysRuntimeCore getRuntime(ExecSite execSite) {
        SysRuntimeCore core = execSiteToRuntime.computeIfAbsent(execSite, this::getFactory);

        return new SysRuntimeCoreWrapperBase(core) {
            @Override
            public void close() {
                // XXX Set closed flag to true.
            }
        };
    }

    public SysRuntimeCore getFactory(ExecSite execSite) {
        ExecSiteVisitorSysRuntimeCoreFactory factory = new ExecSiteVisitorSysRuntimeCoreFactory();
        SysRuntimeCore result = factory.create(execSite);
        return result;
    }

    @Override
    public void close() {
        execSiteToRuntime.values().forEach(x -> IOUtils.closeQuietly(x));
    }

    private class ExecSiteVisitorSysRuntimeCoreFactory
        implements ExecSiteVisitor<SysRuntimeCore> {

        @Override
        public SysRuntimeCore visit(ExecSiteDockerImage execSite) {
            String imageRef = execSite.imageRef();
            SysRuntimeCore result = sysRuntimeDockerFactory.createCore(imageRef);
            return result;
        }

        @Override
        public SysRuntimeCore visit(ExecSiteCurrentHost execSite) {
            return SysRuntimeCoreHost.get();
        }

        @Override
        public SysRuntimeCore visit(ExecSiteCurrentJvm execSite) {
            return SysRuntimeCoreJvm.of(jvmCmdRegistry);
        }

        public SysRuntimeCore create(ExecSite execSite) {
            return execSite.accept(this);
        }
    }
}
