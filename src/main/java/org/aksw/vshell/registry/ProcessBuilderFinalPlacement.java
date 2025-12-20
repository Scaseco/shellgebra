package org.aksw.vshell.registry;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitorRenderAsBashString;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTargetVisitor;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.exec.IProcessBuilderCore;
import org.aksw.shellgebra.exec.ListBuilder;
import org.aksw.shellgebra.exec.ProcessBuilderCore;
import org.aksw.shellgebra.exec.ProcessBuilderDocker;
import org.aksw.shellgebra.exec.ProcessBuilderGroup;
import org.aksw.shellgebra.exec.ProcessBuilderPipeline;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.invocation.InvokableProcessBuilderHost;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentHost;
import org.aksw.shellgebra.exec.model.ExecSiteCurrentJvm;
import org.aksw.shellgebra.exec.model.ExecSiteDockerImage;
import org.aksw.shellgebra.exec.model.ExecSiteVisitor;
import org.aksw.shellgebra.exec.model.ExecSites;

/**
 *
 */
//record Rewrite(
//    PlacedCmd placedCmd,
//    FinalPlacement placement,
//    IProcessBuilderCore<?> processBuilderPrototype,
//    ExecutableInvocation invocation) {
//}

public class ProcessBuilderFinalPlacement
    extends ProcessBuilderCore<ProcessBuilderFinalPlacement>
{
    private FileMapper fileMapper;
    private ExecSiteResolver resolver;
    // private CommandParserCatalog parserCatalog;
    private CommandCatalog commandCatalog;

    private FinalPlacement placement;

    public ProcessBuilderFinalPlacement(FileMapper fileMapper, ExecSiteResolver resolver, CommandCatalog commandCatalog) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
        this.resolver = Objects.requireNonNull(resolver);
        // this.parserCatalog = Objects.requireNonNull(parserCatalog);
        this.commandCatalog = Objects.requireNonNull(commandCatalog);
    }

    public ProcessBuilderFinalPlacement command(FinalPlacement placement) {
        this.placement = placement;
        return self();
    }

    public FinalPlacement command() {
        return placement;
    }

    public FileMapper fileMapper() {
        return fileMapper;
    }

    public ProcessBuilderFinalPlacement fileMapper(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
        return self();
    }

    public ExecSiteResolver execSiteResolver() {
        return resolver;
    }

    public ProcessBuilderFinalPlacement execSiteResolver(ExecSiteResolver resolver) {
        this.resolver = resolver;
        return self();
    }

    @Override
    protected ProcessBuilderFinalPlacement cloneActual() {
        return new ProcessBuilderFinalPlacement(fileMapper, resolver, commandCatalog);
    }

    protected void applySettings(ProcessBuilderFinalPlacement target) {
        target.command(command());
        super.applySettings(target);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        IProcessBuilderCore<?> processBuilder = toProcessBuilder(placement, executor);
        Process result = processBuilder.start(executor);
        return result;
    }

    public IProcessBuilderCore<?> toProcessBuilder(FinalPlacement placement, ProcessRunner context) {
        Map<CmdOpVar, PlacedCmd> varToPlacement = placement.placements();
        ExecutorService executorService = Executors.newCachedThreadPool();
        Dispatcher dispatcher = new Dispatcher(placement, context, commandCatalog, fileMapper, executorService);

        PlacedCmd root = placement.cmdOp();
        IProcessBuilderCore<?> result = dispatcher.resolve(root);
        //CmdOpVisitorToProcessBuilder visitor = new CmdOpVisitorToP(varToPlacement, fileMapper, resolver, executor);
//        PlacedCmd root = placement.cmdOp();
//        IProcessBuilderCore<?> processBuilder = root.accept(visitor);

        // Issue: A process builder should not have active resources - so any resource allocation
        // would have to be deferred until execution.
        // ExecutableInvocation invocation;

        return result;
    }

    @Override
    public boolean supportsAnonPipeRead() {
        return true;
    }

    @Override
    public boolean supportsAnonPipeWrite() {
        return true;
    }

    @Override
    public boolean supportsDirectNamedPipe() {
        return true;
    }
}

class Dispatcher
    implements ExecSiteVisitor<CmdOpVisitor<IProcessBuilderCore<?>>>
{
    private FinalPlacement finalPlacement;
    // private CommandParserCatalog parserCatalog;
    private CommandCatalog commandCatalog;

    private FileMapper fileMapper;
    private ExecutorService executorService;

    private ProcessRunner context;

    private CmdOpVisitorToPbJvm jvmVisitor;
    private CmdOpVisitorToPbHost hostVisitor;

    private Deque<AutoCloseable> closeables = new ArrayDeque<>();

    public Dispatcher(FinalPlacement finalPlacement, ProcessRunner context, CommandCatalog commandCatalog, FileMapper fileMapper, ExecutorService executorService) {
        super();
        this.fileMapper = fileMapper;
        this.context = context;
        // this.parserCatalog = parserCatalog;
        this.commandCatalog = commandCatalog;

        this.finalPlacement = finalPlacement;
        this.executorService = executorService;

        this.jvmVisitor = new CmdOpVisitorToPbJvm(this);
        this.hostVisitor = new CmdOpVisitorToPbHost(this);
    }

    public void addCloseable(AutoCloseable closeable) {
        closeables.add(closeable);
    }

    public ProcessRunner getContext() {
        return context;
    }

    public FileMapper getFileMapper() {
        return fileMapper;
    }

    public CommandCatalog getCommandCatalog() {
        return commandCatalog;
    }

//    public CommandParserCatalog getParserCatalog() {
//        return parserCatalog;
//    }

    @Override
    public CmdOpVisitor<IProcessBuilderCore<?>> visit(ExecSiteDockerImage execSite) {
        return new CmdOpVisitorToPbDocker(this, execSite);
    }

    @Override
    public CmdOpVisitor<IProcessBuilderCore<?>> visit(ExecSiteCurrentHost execSite) {
        return hostVisitor;
    }

    @Override
    public CmdOpVisitor<IProcessBuilderCore<?>> visit(ExecSiteCurrentJvm execSite) {
        return jvmVisitor;
    }

    public IProcessBuilderCore<?> resolve(CmdOpVar v) {
        PlacedCmd placedCmd = finalPlacement.placements().get(v);
        IProcessBuilderCore<?> result = resolve(placedCmd);
        return result;
    }

    public IProcessBuilderCore<?> resolve(PlacedCmd placedCmd) {
        ExecSite execSite = placedCmd.execSite();
        CmdOp cmdOp = placedCmd.cmdOp();
        CmdOpVisitor<IProcessBuilderCore<?>> visitor = execSite.accept(this);
        IProcessBuilderCore<?> result = cmdOp.accept(visitor);
        return result;
    }
}

// Resolve process substitution and redirects.
class CmdArgTransform
    implements CmdArgVisitor<CmdArg>, TokenVisitor<Token>
{
    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
    private Dispatcher dispatcher;

    public CmdArgTransform(CmdOpVisitorToBase cmdOpVisitor) {
        super();
        this.cmdOpVisitor = cmdOpVisitor;
        this.dispatcher = cmdOpVisitor.getDispatcher();
    }

    public static <T> List<T> transformArgs(CmdArgVisitor<T> visitor, List<CmdArg> args) {
        return args.stream().map(token -> token.accept(visitor)).toList();
    }

    public static <T> List<T> transformArgs(TokenVisitor<T> visitor, List<Token> args) {
        return args.stream().map(token -> token.accept(visitor)).toList();
    }

    @Override
    public CmdArg visit(CmdArgRedirect arg) {
        CmdRedirect redirect = arg.redirect();
        RedirectTarget target = redirect.target();
        RedirectTarget newTarget = target.accept(new RedirectTargetVisitor<RedirectTargetFile>() {
            @Override
            public RedirectTargetFile visit(RedirectTargetProcessSubstitution re) {
                CmdOp cmdOp = re.cmdOp();
                Path path = processToPipe(cmdOp);
                return new RedirectTargetFile(path.toString());
            }
            @Override
            public RedirectTargetFile visit(RedirectTargetFile redirect) { return redirect; }
        });
        return new CmdArgRedirect(new CmdRedirect(redirect.fd(), redirect.openMode(), newTarget));
    }

    public Path processVarToPipe(CmdOpVar cmdOp) {
        IProcessBuilderCore<?> processBuilder = dispatcher.resolve(cmdOp);
        return processToPipe(processBuilder);
    }

//    public Path processVarToPipe(PlacedCmd placedCmd) {
//        IProcessBuilderCore<?> processBuilder = dispatcher.resolve(placedCmd);
//        return processToPipe(processBuilder);
//    }

    public Path processToPipe(CmdOp cmdOp) {
        IProcessBuilderCore<?> processBuilder = cmdOp.accept(cmdOpVisitor);
        return processToPipe(processBuilder);
    }

    public Path processToPipe(IProcessBuilderCore<?> processBuilder) {
        Path pipe;
        try {
            pipe = SysRuntime.newNamedPipe();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processBuilder.redirectOutput(new JRedirectJava(Redirect.to(pipe.toFile())));

        Process process = ProcessOverThread.startInThread(processBuilder, dispatcher.getContext());
        dispatcher.addCloseable(() -> process.destroy());
        return pipe;
    }

    @Override
    public CmdArg visit(CmdArgWord arg) {
        return new CmdArgWord(arg.escapeType(), arg.tokens().stream().map(token -> token.accept(this)).toList());
    }

    @Override public Token visit(TokenLiteral token) { return token; }
    @Override public Token visit(TokenPath token) { return token; }

    @Override
    public Token visit(TokenVar token) {
        CmdOpVar cv = new CmdOpVar(token.name());
        Path path = processToPipe(cv);
        return new TokenPath(path.toString());
    }

    @Override
    public Token visit(TokenCmdOp token) {
        CmdOp cmdOp = token.cmdOp();
        Path path = processToPipe(cmdOp);
        return new TokenPath(path.toString());
    }

    @Override
    public CmdArg visit(CmdArgCmdOp arg) {
        CmdOp cmdOp = arg.cmdOp();
        Path path = processToPipe(cmdOp);
        return CmdArg.ofPathString(path.toString());
    }
}

abstract class CmdOpVisitorToBase
    implements CmdOpVisitor<IProcessBuilderCore<?>>
{
    private Dispatcher dispatcher;
    private CmdArgVisitor<CmdArg> cmdArgTransformer;

    public CmdOpVisitorToBase(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
        this.cmdArgTransformer = new CmdArgTransform(this);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    protected abstract IProcessBuilderCore<?> toProcessBuilder(List<String> args);

    @Override
    public IProcessBuilderCore<?> visit(CmdOpExec op) {
        List<CmdArg> args = op.args().args();

        // TODO Deal with arguments that make use of process substitution.
        List<CmdArg> resolvedArgs = CmdArgTransform.transformArgs(cmdArgTransformer, args);
        List<String> resolvedArgStrs = CmdArgVisitorRenderAsBashString.render(resolvedArgs);

        List<String> argv = ListBuilder.ofString().add(op.getName()).addAll(resolvedArgStrs).buildList();
        IProcessBuilderCore<?> result = toProcessBuilder(argv);
        return result;
    }

    @Override
    public IProcessBuilderCore<?> visit(CmdOpPipeline op) {
        List<? extends IProcessBuilderCore<?>> list = op.subOps().stream().map(subOp -> subOp.accept(this)).toList();
        return ProcessBuilderPipeline.of(list);
    }

    @Override
    public IProcessBuilderCore<?> visit(CmdOpGroup op) {
        List<? extends IProcessBuilderCore<?>> list = op.subOps().stream().map(subOp -> subOp.accept(this)).toList();
        return ProcessBuilderGroup.of(list);
    }

    @Override
    public IProcessBuilderCore<?> visit(CmdOpVar op) {
        IProcessBuilderCore<?> result = dispatcher.resolve(op);
        return result;
    }
}

class CmdOpVisitorToPbJvm
    extends CmdOpVisitorToBase {
    public CmdOpVisitorToPbJvm(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    protected IProcessBuilderCore<?> toProcessBuilder(List<String> argv) {
        CommandCatalog commandCatalog = getDispatcher().getCommandCatalog();
        ExecSite execSite = ExecSites.jvm();
        String commandName = argv.get(0);

        String actualCommandName = CmdOpVisitorToPbDocker.resolveOrFail(commandCatalog, commandName, execSite);
        argv = new ArrayList<>(argv);
        argv.set(0, actualCommandName);

        IProcessBuilderCore<?> result = ProcessBuilderJvm.of(argv);
        return result;
    }
}

class CmdOpVisitorToPbHost
    extends CmdOpVisitorToBase {
    public CmdOpVisitorToPbHost(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    protected IProcessBuilderCore<?> toProcessBuilder(List<String> args) {
        IProcessBuilderCore<?> result = InvokableProcessBuilderHost.of(args);
        return result;
    }
}

class CmdOpVisitorToPbDocker
    extends CmdOpVisitorToBase {
    protected ExecSiteDockerImage execSite;

    public CmdOpVisitorToPbDocker(Dispatcher dispatcher, ExecSiteDockerImage execSite) {
        super(dispatcher);
        this.execSite = execSite;
    }

    @Override
    protected IProcessBuilderCore<?> toProcessBuilder(List<String> args) {
        Dispatcher dispatcher = getDispatcher();

        // ProcessRunner context = dispatcher.getContext();
        String commandName = args.get(0);
        // CommandParserCatalog parserCatalog = dispatcher.getParserCatalog();
        CommandCatalog commandCatalog = dispatcher.getCommandCatalog();
        JvmCommandRegistry commandRegistry = dispatcher.getContext().getJvmCmdRegistry();

        // Parser candidates are inferred from the jvm site - whereas the actual command is resolved against the
        // docker exec site.

        JvmCommandParser parser = null;
        Set<String> parserCands = commandCatalog.get(commandName, ExecSites.jvm()).orElse(null);
        String parserCand = null;
        if (parserCands != null) {
            for (String tmp : parserCands) {
                parser = commandRegistry.get(tmp).orElse(null);
                if (parser != null) {
                    parserCand = tmp;
                    break;
                }
            }
        }

        if (parser == null) {
            throw new RuntimeException("No command parser found for: " + commandName);
        }

        // FIXME The actual command should re-use the prior resolution - probably need to bass the resolver or "probe results" tracker here.
        String actualCommandName = resolveOrFail(commandCatalog, commandName, execSite);
        List<String> newArgs = new ArrayList<>(args);
        newArgs.set(0, actualCommandName);


//        JvmCommandParser parser = parserCatalog.getParser(commandName)
////        JvmCommandParser parser = context.getJvmCmdRegistry().get(commandName)
//            .orElseThrow(() -> new RuntimeException("No command parser found for: " + commandName));

        // TODO Resolve command name
        // getDispatcher().getContext().getJvmCmdRegistry().

        String imageRef = execSite.imageRef();
        FileMapper fileMapper = dispatcher.getFileMapper();

        // Issue: We need access to the Args model, especially readsStdin.
        // The CmdOp AST is not sufficient because it does not cover readsStdin (which is an interpretation of the args model).
        // The original command has been resolved, but the args parser was only linked to the original command.
        // Perhaps we can retain the original command - original command + exec site should
        // unambiguously give the actual command.

        IProcessBuilderCore<?> result = ProcessBuilderDocker.of(newArgs)
                .commandParser(parser)
                .imageRef(imageRef)
                .fileMapper(fileMapper)
                ;

        return result;
    }

    public static String resolveOrFail(CommandCatalog commandCatalog, String commandName, ExecSite execSite) {
        // FIXME The actual command should re-use the prior resolution - probably need to bass the resolver or "probe results" tracker here.
        Set<String> nameCands = commandCatalog.get(commandName, execSite)
            .orElseThrow(() -> new RuntimeException("command " + commandName + " not found on exec site " + execSite));
        if (nameCands.isEmpty()) {
            throw new RuntimeException("Command " + commandName + " does not have resolutions on exec site " + execSite);
        }
        String resolvedName = nameCands.iterator().next();
        return resolvedName;
    }
}

/**
 * Resolver for variables in arguments and redirects. Substitutes with
 * variables with pipe file names that have running processes attached.
 */
//class CmdOpVisitorToProcessBuilder
//    implements CmdTransformBase
//{
//    // exec site to preconfigured process builder?
//    // protected IProcessBuilder
//    private ProcessRunner context;
//    private FileMapper fileMapper;
//    private Map<CmdOpVar, PlacedCmd> varToPlacement;
//    private ExecSiteResolver resolver;
//    private ExecutorService executor;
//
//    // Paths that are written to by processes that we created.
//    private Map<Object, Process> pipeToProcess = new ConcurrentHashMap<>();
//
//    public CmdOpVisitorToProcessBuilder(ProcessRunner context, FileMapper fileMapper, Map<CmdOpVar, PlacedCmd> varToPlacement, ExecSiteResolver resolver, ExecutorService executor) {
//        super();
//        this.context = context;
//        this.fileMapper = fileMapper;
//        this.varToPlacement = varToPlacement;
//        this.resolver = resolver;
//        this.executor = executor;
//    }
//
//    /**
//     * Pipelines may be transformed into groups where statements have redirects with pipes.
//     */
//    @Override
//    public CmdOp transform(CmdOpPipeline op, List<CmdOp> subOps) {
//        boolean hasVars = subOps.stream().anyMatch(CmdOp::isVar);
//        CmdOp result;
//        if (!hasVars) {
//            // If there are no vars there is nothing to do.
//            result = new CmdOpPipeline(subOps);
//
//            // XXX Should we push down to a Invocation.Script string here?
//
//        } else {
//        }
//        return result;
//    }
//
//    /**
//     * @throws IOException
//     */
//    public CmdOp transformPipeline(List<CmdOp> subOps) throws IOException {
//        Path pipe = SysRuntime.newNamedPipe();
//        List<IProcessBuilderCore<?>> processBuilders = toProcessBuilders(subOps);
//        ProcessBuilderPipeline bp = ProcessBuilderPipeline.of(processBuilders);
//        bp.redirectOutput(new JRedirectJava(Redirect.to(pipe.toFile())));
//        return CmdOps.exec("cat", CmdArg.ofPathString(pipe.toString()));
//    }
//
//    public List<IProcessBuilderCore<?>> toProcessBuilders(List<CmdOp> subOps) {
//    	dispatcher
//    }
//
////    // Note: all statements of the group (except for perhaps the last) need to run in the background!
////    public CmdOp transformPipelineToGroup(List<CmdOp> subOps) {
////        // One option is to
////
////        List<CmdOp> group = new ArrayList<>();
////        for (CmdOp subOp : subOps) {
////            if (subOp.isVar()) {
////
////            }
////        }
////    }
//
//    @Override
//    public CmdArg transform(CmdArgCmdOp arg, CmdOp subOp) {
//        throw new UnsupportedOperationException("Process output to argument array not supported yet.");
//    }
//
//    @Override
//    public CmdArg transform(CmdArgRedirect arg) {
//        CmdRedirect cmdRedirect = arg.redirect();
//        RedirectTarget target = cmdRedirect.target();
//        CmdArg outArg = target.accept(new RedirectTargetVisitor<CmdArg>() {
//            @Override
//            public CmdArg visit(RedirectTargetFile redirect) {
//                return arg;
//            }
//
//            // If the target is a var, then execution happens on another site.
//            // If the target is a cmdOp expression that makes use of a variable
//            // This means: Create named pipe, create processBuilder for the command backed by the var,
//            //             set the named pipe on the processBuilder and start the process.
//            //             if the process builder is a 'docker' one, then it will mount the named pipe
//            //             into the container.
//            @Override
//            public CmdArg visit(RedirectTargetProcessSubstitution redirect) {
//                CmdOp op = redirect.cmdOp();
//                Set<CmdOpVar> vars = CmdOps.accVars(op);
//                if (vars.isEmpty()) {
//                    // Return the arg itself because it can be turned into a script string
//                    // together with the command for which it is the redirect.
//                    return arg;
//                } else {
//                    // There are variables - so things get executed on one or more different sites.
//                    // Set up an anonymous pipe. Create a java thread that executes the expression.
//                    throw new UnsupportedOperationException("handling of redirects with vars not yet implemented.");
//                }
//            }
//        });
//        return outArg;
//    }
//
//    protected CmdOpVisitorToProcessBuilder newSubVisitor() {
//        return new CmdOpVisitorToProcessBuilder(context, fileMapper, varToPlacement, resolver, executor);
//    }
//
//    /**
//     * Vars can appear as pipeline or group members.
//     * They become "cat named-pipe-read-end" expressions:
//     * site0:[cat foo] | site1:[lbzip2]
//     * becomes
//     *   site0[cat namedPipe[readEnd]]
//     *   with:
//     *     site0[cat foo > anonPipe(0)[writeEnd]]
//     *     site1[cat anonPipe(0)[readEnd] | lbzip2 > namePipe[writeEnd]]
//     * TODO If the pipeline goes into a container then avoid the anonPipe!
//     *      Implication: Need API to query for supported pipe types.
//     */
//    @Override
//    public CmdOp transform(CmdOpVar op) {
//        PlacedCmd placement = varToPlacement.get(op);
//        PosixPipe pipe = PosixPipe.open();
//
//        ExecSite execSite = placement.execSite();
//        CmdOp cmdOp = placement.cmdOp();
//
//        // TODO how to pass on the pipe? it needs to end up as a redirect
//        // on the sub-process builder.
//        Consumer<IProcessBuilderCore<?>> configurer = pb ->
//            pb.redirectOutput(new JRedirectJava(Redirect.to(pipe.getWriteEndProcFile())));
//
//        CmdOpVisitorToProcessBuilder subVisitor = newSubVisitor();
//        CmdOp outOp = CmdOpTransformer.transform(cmdOp, subVisitor);
//
//        CmdOp result = CmdOps.exec("cat", CmdArg.ofPathString(pipe.getReadEndProcPath().toString()));
//        return result;
//    }
//
//    @Override
//    public Token transform(TokenCmdOp token, CmdOp subOp) {
//        Set<CmdOpVar> vars = CmdOps.accVars(subOp);
//        if (vars.isEmpty()) {
//            return CmdTransformBase.super.transform(token, subOp);
//        } else {
//
//        }
//    }
//
//    protected Path resolve(CmdOp cmdOp) {
//        PosixPipe pipe = PosixPipe.open();
//        IProcessBuilderCore<?> processBuilder = toProcessBuilder(cmdOp);
//        processBuilder.redirectOutput(new JRedirectJava(Redirect.to(pipe.getWriteEndProcFile())));
//        Process process = processBuilder.start(context);
//        pipeToProcess.put(pipe, process);
//        return pipe.getReadEndProcPath();
//    }
//
//    protected IProcessBuilderCore<?> toProcessBuilder(ExecSite execSite, CmdOpExec cmdOp) {
//
//        // resolver.
//    }



//    @Override
//    public IProcessBuilderCore<?> visit(CmdOpExec op) {
//        // CmdOpVisitorToProcessBuilder self = this;
//        op.name();
//        ArgumentList args = op.args();
//        List<String> finalArgs = new ArrayList<>(args.size());
//
////        List<String> r = arg.tokens().stream().flatMap(token -> {
////
////        .toList();
////                return r;
//
//        for (CmdArg arg : args.args()) {
//            // TODO Probably redirect args need to be handled separately from command args.
//        }
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//}


//
//
//class CmdArgVisitorToProcessBuilder
//    implements CmdArgVisitor<CmdArg> {
//
//    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
//    private TokenVisitor<List<Token>> tokenVisitor;
//    private RedirectTargetVisitor<CmdArg> redirectTargetVisitor;
//
//    // Processes attached to tokens.
//    private Map<Token, Process> tokenToProcess;
//
//    @Override
//    public CmdArg visit(CmdArgCmdOp arg) {
//        CmdOp cmdOp = arg.cmdOp();
//        IProcessBuilderCore<?> result = cmdOp.accept(cmdOpVisitor);
//        return result;
//    }
//
//    @Override
//    public CmdArg visit(CmdArgRedirect arg) {
//        CmdRedirect cmdRedirect = arg.redirect();
//        RedirectTarget redirectTarget = cmdRedirect.target();
//        CmdArg result = redirectTarget.accept(redirectTargetVisitor);
//        return result;
//    }
//
//    @Override
//    public CmdArg visit(CmdArgWord arg) {
//        List<Token> inTokens = arg.tokens();
//        List<Token> outTokens = inTokens.stream().flatMap(token -> {
//            List<Token> r = token.accept(tokenVisitor);
//            return r.stream();
//        }).toList();
//        return new CmdArgWord(arg.escapeType(), outTokens);
//    }
//}
//
//
//class TokenVisitorToProcessBuilder
//    implements TokenVisitor<Token>
//{
//    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
//    private ProcessRunner executor;
//
//    @Override
//    public Token visit(TokenLiteral token) {
//        return token;
//    }
//
//    @Override
//    public Token visit(TokenPath token) {
//        return token;
//    }
//
//    @Override
//    public Token visit(TokenVar token) {
//        throw new UnsupportedOperationException("Variables not yet supported: " + token);
//    }
//
//    @Override
//    public Token visit(TokenCmdOp token) {
//        if (true) {
//            throw new UnsupportedOperationException("Process substitution not yet supported");
//        }
//
//        try {
//            CmdOp subCmdOp = token.cmdOp();
//            // So the problem is that we are creating a process builder of which some parts are
//            // already being executed.
//            IProcessBuilderCore<?> pb = subCmdOp.accept(cmdOpVisitor);
//            Path namedPipe = SysRuntime.newNamedPipe();
//            pb.redirectOutput(new JRedirectJava(Redirect.to(namedPipe.toFile())));
//            Supplier<Integer> supplier = () -> {
//                try {
//                    try {
//                        Process p = pb.start(executor);
//                        int exitValue = p.waitFor();
//                        return exitValue;
//                    } finally {
//                        Files.deleteIfExists(namedPipe);
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            };
//            Thread thread = new Thread(() -> { supplier.get(); });
//            thread.start();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        return null;
//        // TODO Register this child-process with the current execution.
//        // TODO Wrap the process such that on exit the pipe gets removed again.
//        // Also, return the name of the pipe.
//
//        // TODO We need some API/builder or helper to launch a process in another thread,
//        // link resources to it, and free them once the process terminates.
//
//        // TODO Allocate a named pipe and configure process builder with it.
////        return List.of(namedPipe.toString());
////            }
//    }
//}
//
//
//class RedirectTargetVisitorToProcessBuilder
//    implements RedirectTargetVisitor<RedirectTarget>
//{
//    /** The redirect. Needed as context for whether the target is read from or written to. */
//    private CmdArgRedirect redirect;
//
//    @Override
//    public RedirectTarget visit(RedirectTargetFile redirect) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public RedirectTarget visit(RedirectTargetProcessSubstitution redirect) {
//        CmdOp cmdOp = redirect.cmdOp();
//        throw new UnsupportedOperationException("ProcessSubstitution not supported yet");
//    }
//}

//class PlacedCmdOpVisitorToStage
//    implements PlacedCmdOpVisitor<IProcessBuilderCore<?>>
//{
//    private Map<CmdOpVar, PlacedCmd> varToPlacement;
//    private FileMapper fileMapper;
//    private ExecSiteResolver resolver;
//
//    private ProcessRunner executor;
//
//    public PlacedCmdOpVisitorToStage(Map<CmdOpVar, PlacedCmd> varToPlacement, FileMapper fileMapper, ExecSiteResolver resolver) {
//        super();
//        this.varToPlacement = varToPlacement;
//        this.fileMapper = fileMapper;
//        this.resolver = resolver;
//    }
//
//    //private JvmCommandRegistry jvmCmdRegistry;
//    //private FileMapper fileMapper;
//
//    //public PlacedCmdOpVisitorToStage(Map<CmdOpVar, PlacedCmd> varToPlacement, JvmCommandRegistry jvmCmdRegistry, FileMapper fileMapper) {
//    //    super();
//    //    this.varToPlacement = varToPlacement;
//    //    this.jvmCmdRegistry = jvmCmdRegistry;
//    //    this.fileMapper = fileMapper;
//    //}
//
//    public IProcessBuilderCore<?> resolveVar(CmdOpVar cmdOpVar) {
//        PlacedCmd placedCmd = varToPlacement.get(cmdOpVar);
//        if (placedCmd == null) {
//            throw new RuntimeException("Unresolvable variable: " + cmdOpVar);
//        }
//
//        ProcessBuilder result = placedCmd.accept(this);
//        return result;
//    }
//
//    @Override
//    public IProcessBuilderCore<?> visit(PlacedCmd op) {
//        CmdOp cmdOp = op.cmdOp();
//        Set<CmdOpVar> cmdOpVars = CmdOps.accVars(cmdOp);
//
//        // For each variable: Generate the stage for its definition.
//        // Then substitute the variable with a named pipe for that stage.
//
//        Map<CmdOpVar, Stage> varToStage = new HashMap<>();
//        // Map<CmdOpVar, String> varToFileName = new HashMap<>();
//        // Map<CmdOpVar, FileWriterTask> varToFileWriterTask = new HashMap<>();
//        Map<CmdOpVar, TokenPath> tokenToPath = new HashMap<>();
//        for (CmdOpVar v : cmdOpVars) {
//            PlacedCmd childPlacement = varToPlacement.get(v);
//
//            // PlacedCmdOpToStage subVisitor = new PlacedCmdOpToStage();
//            Stage stage = childPlacement.accept(this);
//
//            String pathStr = fileMapper.allocateTempFilename("tmpfile", "pipe");
//            tokenToPath.put(v, new TokenPath(pathStr));
//            // CmdArg cmdArgPath = CmdArg.ofPathString(pathStr);
//
//            // stage.for
//            varToStage.put(v, stage);
//            // varToPlacement.put(v, op)
//        }
//
//        CmdOp finalCmdOp = CmdOpTransformer.transform(cmdOp, null, new CmdArgTransformBase() {
//            @Override
//            public CmdArg transform(CmdArgCmdOp token, CmdOp subOp) {
//                Token t = subOp instanceof CmdOpVar v
//                    ? tokenToPath.get(v)
//                    : null;
//                CmdArg r = t == null
//                    ? super.transform(token, subOp)
//                    : new CmdArgWord(StringEscapeType.ESCAPED, t);
//                return r;
//            }
//        }, null);
//
//        ExecSite execSite = op.getExecSite();
//        Stage stage = execSite.accept(new ExecSiteVisitor<Stage>() {
//            @Override
//            public Stage visit(ExecSiteDockerImage execSite) {
//
//
//
//                String imageRef = execSite.imageRef();
//                Stage r = Stages.docker(imageRef, finalCmdOp, fileMapper, PlacedCmdOpVisitorToStage.this::resolveVar);
//                return r;
//            }
//
//            @Override
//            public Stage visit(ExecSiteCurrentHost execSite) {
//                Stage r = Stages.host(finalCmdOp);
//                return r;
//            }
//
//            @Override
//            public Stage visit(ExecSiteCurrentJvm execSite) {
//                // CommandCatalog cmdCatalog = resolver.getCommandCatalog();
//                JvmCommandRegistry jvmCmdRegistry = resolver.getJvmCmdRegistry();
//                // cmdCatalog.get(finalCmdOp
//
//                CmdOpVisitor<Stage> cmdOpVisitorToStage = new CmdOpVisitorExecJvm(resolver, PlacedCmdOpVisitorToStage.this::resolveVar);
//                Stage r = finalCmdOp.accept(cmdOpVisitorToStage);
//                return r;
//            }
//        });
//        return stage;
//    }
//
//    /**
//     * Produces a stage that concatenates the output of all stages.
//     * Only supports ExecSiteCurrentJvm which concats the ByteSources.
//     * To use host or docker, groups and pipelines should be transformed to PlaceCmd where the cmd is a bash command
//     * that implements the group/pipeline.
//     */
//    @Override
//    public IProcessBuilderCore<?> visit(PlacedGroup op) {
//        List<PlacedCmdOp> subOps = op.subOps();
//        List<IProcessBuilderCore<?>> stages = toStages(subOps);
//
//        ExecSite execSite = op.getExecSite();
//        Stage stage = execSite.accept(new ExecSiteVisitor<Stage>() {
//            @Override
//            public Stage visit(ExecSiteDockerImage execSite) {
//                throw new UnsupportedOperationException();
//    //                String imageRef = execSite.imageRef();
//    //                Stage r = Stages.docker(imageRef, cmdOp, fileMapper);
//    //                return r;
//            }
//
//            // Could turn all output into named pipes and use bash process to concat it.
//            // /bin/cat < namedPipe1 < namedPipe2
//            @Override
//            public Stage visit(ExecSiteCurrentHost execSite) {
//                throw new UnsupportedOperationException();
//                // Stage r = Stages.host(cmdOp);
//                // return r;
//            }
//
//            @Override
//            public Stage visit(ExecSiteCurrentJvm execSite) {
//                List<ByteSource> byteSources = new ArrayList<>(stages.size());
//                for (Stage stage : stages) {
//                    ByteSource bs = stage.fromNull().toByteSource();
//                    byteSources.add(bs);
//                }
//                ByteSource concat = ByteSource.concat(byteSources);
//                // Note: This stage ignores input data and serves the group.
//                //   TODO: Perhaps attach in to first member of the group? - It's a corner case.
//                Stage r = Stages.javaIn(in -> concat.openStream());
//                return r;
//            }
//        });
//        return stage;
//    }
//
//    protected List<IProcessBuilderCore<?>> toStages(List<PlacedCmdOp> subOps) {
//        List<IProcessBuilderCore<?>> result = new ArrayList<>(subOps.size());
//        for (PlacedCmdOp subOp : subOps) {
//            IProcessBuilderCore<?> stage = subOp.accept(this);
//            result.add(stage);
//        }
//        return result;
//    }
//
//    @Override
//    public IProcessBuilderCore<?> visit(PlacedPipeline op) {
//        List<PlacedCmdOp> subOps = op.subOps();
//        List<IProcessBuilderCore<?>> processBuilders = toStages(subOps);
//        // Stage result = Stages.pipeline(stages);
//        IProcessBuilderCore<?> result = new ProcessBuilderPipeline().processBuilders(processBuilders);
//        return result;
//    }
//}

//public void resolveExecSite(ExecSite execSite) {
//    // So the problem is that directly returning a process builder does not work, because
//    // we don't know what command object we are going to pass to it.
//    // Background: I am not sure whether all commands for process builders need to be of type String[].
//    // E.g. A process builder pipeline or group just accept a list of sub-process builders.
//    // But "leaf" process builders probably always use String[].
//    // We could have a process builder wrapper where the command is an ExecutableInvocation.
//    // Hm, I rather keep that separate - the result of a PlacedCmdExec transformation should be:
//    //   (1) Here is an ExecutableInvocation
//    //   (2) Here is a process builder that would execute it.
//    // The essential aspect of the process builder is the configurability of redirects.
//    // So we have ExecutableInvocation: Argv + Closable
//    // Perhaps ActiveProcessBuilder: ProcessBuilder + Closable.
//    // resolver.newProcessBuilderShim()
//}
//

