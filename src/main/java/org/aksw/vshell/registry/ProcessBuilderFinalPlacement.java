package org.aksw.vshell.registry;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgRedirect;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgVisitor;
import org.aksw.shellgebra.algebra.cmd.arg.CmdArgWord;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenCmdOp;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenLiteral;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTargetVisitor;
import org.aksw.shellgebra.algebra.cmd.transform.FileMapper;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdTransformBase;
import org.aksw.shellgebra.exec.IProcessBuilderCore;
import org.aksw.shellgebra.exec.ProcessBuilderCore;
import org.aksw.shellgebra.exec.SysRuntime;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.exec.graph.PosixPipe;
import org.aksw.shellgebra.exec.graph.ProcessRunner;
import org.aksw.shellgebra.exec.model.ExecSite;

public class ProcessBuilderFinalPlacement
    extends ProcessBuilderCore<ProcessBuilderFinalPlacement>
{
    private FileMapper fileMapper;
    private ExecSiteResolver resolver;

    private FinalPlacement placement;

    public ProcessBuilderFinalPlacement(FileMapper fileMapper, ExecSiteResolver resolver) {
        super();
        this.fileMapper = Objects.requireNonNull(fileMapper);
        this.resolver = Objects.requireNonNull(resolver);
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
        return new ProcessBuilderFinalPlacement(fileMapper, resolver);
    }

    protected void applySettings(ProcessBuilderFinalPlacement target) {
        target.command(command());
        super.applySettings(target);
    }

    @Override
    public Process start(ProcessRunner executor) throws IOException {
        IProcessBuilderCore<?> processBuilder = toProcessBuilder(placement);
        Process result = processBuilder.start(executor);
        return result;
    }

    public IProcessBuilderCore<?> toProcessBuilder(FinalPlacement placement) {
        Map<CmdOpVar, PlacedCmd> varToPlacement = placement.placements();
        CmdOpVisitorToProcessBuilder visitor = new CmdOpVisitorToProcessBuilder(varToPlacement, fileMapper, resolver, executor);
        PlacedCmd root = placement.cmdOp();
        IProcessBuilderCore<?> processBuilder = root.accept(visitor);
        return processBuilder;
    }
}


/**
 * Resolver for variables in arguments and redirects. Substitutes with
 * variables with pipe file names that have running processes attached.
 */
class CmdOpVisitorToProcessBuilder
    implements CmdTransformBase
{
    // exec site to preconfigured process builder?
    // protected IProcessBuilder
    private ProcessRunner executor;
    private FileMapper fileMapper;
    private Map<CmdOpVar, PlacedCmd> varToPlacement;
    private ExecSiteResolver resolver;

    // Paths that are written to by processes that we created.
    private Map<Object, Process> pipeToProcess = new ConcurrentHashMap<>();

    public CmdOpVisitorToProcessBuilder(ProcessRunner executor, FileMapper fileMapper, Map<CmdOpVar, PlacedCmd> varToPlacement, ExecSiteResolver resolver) {
        super();
        this.executor = executor;
        this.fileMapper = fileMapper;
        this.varToPlacement = varToPlacement;
        this.resolver = resolver;
    }

    @Override
    public CmdArg transform(CmdArgCmdOp arg, CmdOp subOp) {
        throw new UnsupportedOperationException("Process output to argument array not supported yet.");
    }

    @Override
    public CmdArg transform(CmdArgRedirect arg) {
        CmdRedirect cmdRedirect = arg.redirect();
        RedirectTarget target = cmdRedirect.target();
        CmdArg outArg = target.accept(new RedirectTargetVisitor<CmdArg>() {
            @Override
            public CmdArg visit(RedirectTargetFile redirect) {
                return arg;
            }

            // If the target is a var, then execution happens on another site.
            // If the target is a cmdOp expression that makes use of a variable
            // This means: Create named pipe, create processBuilder for the command backed by the var,
            //             set the named pipe on the processBuilder and start the process.
            //             if the process builder is a 'docker' one, then it will mount the named pipe
            //             into the container.
            @Override
            public CmdArg visit(RedirectTargetProcessSubstitution redirect) {
                CmdOp op = redirect.cmdOp();
                Set<CmdOpVar> vars = CmdOps.accVars(op);
                if (vars.isEmpty()) {
                    // Return the arg itself because it can be turned into a script string
                    // together with the command for which it is the redirect.
                    return arg;
                } else {
                    // There are variables - so things get executed on one or more different sites.
                    // Set up an anonymous pipe. Create a java thread that executes the expression.
                    throw new UnsupportedOperationException("handling of redirects with vars not yet implemented.");
                }
            }
        });
        return outArg;
    }

    @Override
    public CmdOp transform(CmdOpVar op) {
        PlacedCmd placement = varToPlacement.get(op);
        PosixPipe pipe = PosixPipe.open();


    }

    @Override
    public Token transform(TokenCmdOp token, CmdOp subOp) {
        Set<CmdOpVar> vars = CmdOps.accVars(subOp);
        if (vars.isEmpty()) {
            return CmdTransformBase.super.transform(token, subOp);
        } else {

        }
    }

    protected Path resolve(CmdOp cmdOp) {
        PosixPipe pipe = PosixPipe.open();
        IProcessBuilderCore<?> processBuilder = toProcessBuilder(cmdOp);
        processBuilder.redirectOutput(new JRedirectJava(Redirect.to(pipe.getWriteEndProcFile())));
        Process process = processBuilder.start(executor);
        pipeToProcess.put(pipe, process);
        return pipe.getReadEndProcPath();
    }

    protected IProcessBuilderCore<?> toProcessBuilder(ExecSite execSite, CmdOpExec cmdOp) {

        // resolver.
    }



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

}




class CmdArgVisitorToProcessBuilder
    implements CmdArgVisitor<CmdArg> {

    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
    private TokenVisitor<List<Token>> tokenVisitor;
    private RedirectTargetVisitor<CmdArg> redirectTargetVisitor;

    // Processes attached to tokens.
    private Map<Token, Process> tokenToProcess;

    @Override
    public CmdArg visit(CmdArgCmdOp arg) {
        CmdOp cmdOp = arg.cmdOp();
        IProcessBuilderCore<?> result = cmdOp.accept(cmdOpVisitor);
        return result;
    }

    @Override
    public CmdArg visit(CmdArgRedirect arg) {
        CmdRedirect cmdRedirect = arg.redirect();
        RedirectTarget redirectTarget = cmdRedirect.target();
        CmdArg result = redirectTarget.accept(redirectTargetVisitor);
        return result;
    }

    @Override
    public CmdArg visit(CmdArgWord arg) {
        List<Token> inTokens = arg.tokens();
        List<Token> outTokens = inTokens.stream().flatMap(token -> {
            List<Token> r = token.accept(tokenVisitor);
            return r.stream();
        }).toList();
        return new CmdArgWord(arg.escapeType(), outTokens);
    }
}


class TokenVisitorToProcessBuilder
    implements TokenVisitor<Token>
{
    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
    private ProcessRunner executor;

    @Override
    public Token visit(TokenLiteral token) {
        return token;
    }

    @Override
    public Token visit(TokenPath token) {
        return token;
    }

    @Override
    public Token visit(TokenVar token) {
        throw new UnsupportedOperationException("Variables not yet supported: " + token);
    }

    @Override
    public Token visit(TokenCmdOp token) {
        if (true) {
            throw new UnsupportedOperationException("Process substitution not yet supported");
        }

        try {
            CmdOp subCmdOp = token.cmdOp();
            // So the problem is that we are creating a process builder of which some parts are
            // already being executed.
            IProcessBuilderCore<?> pb = subCmdOp.accept(cmdOpVisitor);
            Path namedPipe = SysRuntime.newNamedPipe();
            pb.redirectOutput(new JRedirectJava(Redirect.to(namedPipe.toFile())));
            Supplier<Integer> supplier = () -> {
                try {
                    try {
                        Process p = pb.start(executor);
                        int exitValue = p.waitFor();
                        return exitValue;
                    } finally {
                        Files.deleteIfExists(namedPipe);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            Thread thread = new Thread(() -> { supplier.get(); });
            thread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
        // TODO Register this child-process with the current execution.
        // TODO Wrap the process such that on exit the pipe gets removed again.
        // Also, return the name of the pipe.

        // TODO We need some API/builder or helper to launch a process in another thread,
        // link resources to it, and free them once the process terminates.

        // TODO Allocate a named pipe and configure process builder with it.
//        return List.of(namedPipe.toString());
//            }
    }
}


class RedirectTargetVisitorToProcessBuilder
    implements RedirectTargetVisitor<RedirectTarget>
{
    /** The redirect. Needed as context for whether the target is read from or written to. */
    private CmdArgRedirect redirect;

    @Override
    public RedirectTarget visit(RedirectTargetFile redirect) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RedirectTarget visit(RedirectTargetProcessSubstitution redirect) {
        CmdOp cmdOp = redirect.cmdOp();
        throw new UnsupportedOperationException("ProcessSubstitution not supported yet");
    }
}

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
