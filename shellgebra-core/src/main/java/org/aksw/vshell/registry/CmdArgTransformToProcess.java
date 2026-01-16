package org.aksw.vshell.registry;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.List;

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
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.redirect.CmdRedirect;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetFile;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTarget.RedirectTargetProcessSubstitution;
import org.aksw.shellgebra.algebra.cmd.redirect.RedirectTargetVisitor;
import org.aksw.shellgebra.exec.graph.JRedirect.JRedirectJava;
import org.aksw.shellgebra.pipe.NamedPipe;
import org.aksw.shellgebra.processbuilder.IProcessBuilderCore;

/**
 * Resolve process substitution and redirects.
 *
 * Substitutes {@link CmdArg} instances of process substitutions with paths that are written to by running processes.
 * The essential method is processToPipe.
 * The running processes are registered with the dispatcher.
 */
public class CmdArgTransformToProcess
    implements CmdArgVisitor<CmdArg>, TokenVisitor<Token>
{
    private CmdOpVisitor<IProcessBuilderCore<?>> cmdOpVisitor;
    private ExecSiteToProcessDispatcher dispatcher;

    public CmdArgTransformToProcess(CmdOpVisitorToBase cmdOpVisitor) {
        super();
        this.cmdOpVisitor = cmdOpVisitor;
        this.dispatcher = cmdOpVisitor.getDispatcher();
    }

    public static <T> List<T> transformArgs(TokenVisitor<T> visitor, List<Token> tokens) {
        return tokens.stream().map(token -> transform(visitor, token)).toList();
    }

    public static <T> T transform(TokenVisitor<T> visitor, Token token) {
        T result = token.accept(visitor);
        return result;
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

    public Path processToPipe(CmdOp cmdOp) {
        IProcessBuilderCore<?> processBuilder = cmdOp.accept(cmdOpVisitor);
        return processToPipe(processBuilder);
    }

    public Path processToPipe(IProcessBuilderCore<?> processBuilder) {
        Path pipe;
        try {
            pipe = NamedPipe.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processBuilder.redirectOutput(new JRedirectJava(Redirect.to(pipe.toFile())));

        Process process = ProcessOverThread.startInThread(processBuilder, dispatcher.getContext());
        // dispatcher.addCloseable(() -> process.destroy());
        dispatcher.addProcess(process);
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
