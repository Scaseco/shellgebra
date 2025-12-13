package org.aksw.shellgebra.exec.invocation;

//public class InvokableProcessBuilderDocker
//    extends InvokableProcessBuilderBase<InvokableProcessBuilderDocker>
//{
//    private static final Logger logger = LoggerFactory.getLogger(InvokableProcessBuilderDocker.class);
//
//    /** If unset then fall back to {@link InvocationCompilerImpl#getDefault()}. */
//    private InvocationCompiler compiler = null;
//
//    protected String imageRef;
//    protected String entrypoint;
//    protected String workingDirectory;
//    protected ContainerPathResolver containerPathResolver;
//    protected FileMapper fileMapper;
//    protected boolean interactive;
//
//    public InvokableProcessBuilderDocker compiler(InvocationCompiler compiler) {
//        this.compiler = compiler;
//        return self();
//    }
//
//    public InvocationCompiler compiler() {
//        return compiler;
//    }
//
//    @Override
//    public Process start(ProcessRunner executor) throws IOException {
//        Objects.requireNonNull(imageRef, "image not set.");
//
//        // Resolver over SysRuntime. Container is started only on-demand.
//        CompileContext cxt = CompileContext.of(resolver -> {
//            try (SysRuntime runtime = SysRuntimeCoreLazy.of(() -> SysRuntimeFactoryDocker.create().create(imageRef))) {
//                try {
//                    String resolvedCommand = runtime.which(entrypoint);
//                    return resolvedCommand;
//                } catch (IOException | InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        Invocation inv = invocation();
//        if (inv == null) {
//            throw new IllegalStateException("No invocation set");
//        }
//
//        InvocationCompiler finalCompiler = compiler != null ? compiler : InvocationCompilerImpl.getDefault();
//        ExecutableInvocation exec = finalCompiler.compile(inv, cxt);
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command(exec.argv());
//        ProcessBuilderNative.configure(pb, this, executor);
//        Process p = pb.start();
//        // Cleanup after process exit.
//        p.toHandle().onExit().thenRun(() -> {
//            try {
//                exec.close();
//            } catch (Exception e) {
//                logger.warn("Error during close", e);
//            }
//        });
//        return p;
//    }
//}
