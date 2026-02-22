package org.aksw.shellgebra.exec;

import java.time.Duration;

import org.aksw.commons.util.ref.Ref;
import org.aksw.shellgebra.exec.resource.ResourceMgr;

public class SysRuntimeCoreFactoryPoolDocker
    implements AutoCloseable
{
    private SysRuntimeFactoryDocker sysRuntimeDockerFactory;
    private ResourceMgr<String, SysRuntimeCore> resourceMgr;

    protected SysRuntimeCoreFactoryPoolDocker(SysRuntimeFactoryDocker sysRuntimeDockerFactory) {
        super();
        this.sysRuntimeDockerFactory = sysRuntimeDockerFactory;
        this.resourceMgr = ResourceMgr.of(imageRef -> sysRuntimeDockerFactory.createCore(imageRef),
                SysRuntimeCore::close,
                Duration.ofSeconds(15));
    }

    public static SysRuntimeCoreFactoryPoolDocker of() {
        return of(SysRuntimeFactoryDocker.get());
    }

    public static SysRuntimeCoreFactoryPoolDocker of(SysRuntimeFactoryDocker sysRuntimeDockerFactory) {
        return new SysRuntimeCoreFactoryPoolDocker(sysRuntimeDockerFactory);
    }

    // Instances must be closed after use!
    // @Override
    public SysRuntimeCore getRuntime(String imageRef) {
        Ref<SysRuntimeCore> ref = resourceMgr.get(imageRef);

        return new SysRuntimeCoreWrapperBase(null) {
            @Override
            public SysRuntimeCore getDelegate() {
                return ref.get();
            }

            @Override
            public void close() {
                ref.close();
            }
        };
    }

//    public SysRuntimeCore getFactory(String imageRef) {
//        return sysRuntimeDockerFactory.createCore(imageRef);
//    }

    @Override
    public void close() {
        resourceMgr.close();
    }
}
