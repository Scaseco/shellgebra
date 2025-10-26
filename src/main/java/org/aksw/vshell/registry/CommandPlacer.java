package org.aksw.vshell.registry;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.PlacedCommand;


public class CommandPlacer
    implements CmdOpVisitor<PlacedCommand>
{
    /** ExecSiteResolver can test exec sites for whether they provide a command. */
    // private CommandReg
    private ExecSiteResolver execSiteResolver;
    private Set<ExecSite> preferredExecSites;
    private Map<CmdOp, Set<ExecSite>> opToSites = new IdentityHashMap<>();

    @Override
    public PlacedCommand visit(CmdOpExec op) {
        // Allocate the op's to-be-filled set of exec sites.
        Set<ExecSite> execSites = opToSites.computeIfAbsent(op, k -> new HashSet<>());

        String cmdName = op.getName();

        // Check all preferred exec sites for whether they provide the command.
        for (ExecSite execSite : preferredExecSites) {
            boolean isCmdPresent = execSiteResolver.providesCommand(cmdName, execSite);
            if (isCmdPresent) {
                execSites.add(execSite);
            }
        }

        // If no exec sites were found, check the command registry for
        // the exec site that provides the command.
        if (execSites.isEmpty()) {

        }

        // Whether to check all sub-op images for whether it contains the command.

        boolean checkPresenceInImages = true;

        // Check which docker images provide the command.





        return null;
    }

    @Override
    public PlacedCommand visit(CmdOpPipeline op) {


        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlacedCommand visit(CmdOpGroup op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlacedCommand visit(CmdOpVar op) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlacedCommand visit(CmdOpToArg op) {
        // TODO Auto-generated method stub
        return null;
    }
}
