package org.aksw.vshell.registry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpToArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.exec.Stage;
import org.aksw.shellgebra.exec.Stages;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.PlacedCommand;
import org.apache.hadoop.util.Sets;

public class PlacedCommandProcessor
    implements CmdOpVisitor<PlacedCmdOp>
{
    // private Map<CmdOp, ExecSite> opToExecSite;
    private PlacedCommand root;
    private Map<CmdOpVar, PlacedCommand> inVarToCandPlacement;
    private Map<CmdOpVar, PlacedCmd> outVarToPlacement;

    class Processor
        implements CmdOpVisitor<PlacedCmdOp>
    {
        private Set<ExecSite> execSites;

        public Processor(Set<ExecSite> execSites) {
            super();
            this.execSites = execSites;
        }

        @Override
        public PlacedCmdOp visit(CmdOpExec op) {

            return null;
        }

        @Override
        public PlacedCmdOp visit(CmdOpPipeline op) {
            Set<CmdOpVar> mentionedVars = CmdOps.accVars(op);

            Set<ExecSite> parentSites = inVarToCandPlacement.get(op);
            List<CmdOp> subOps = op.getSubOps();
            ExecSite selectedSite = null;
            for (CmdOp subOp : subOps) {
                PlacedCommand childPc = inVarToCandPlacement.get(subOps);
                Set<ExecSite> childSites = childPc.execSites();

                Set<ExecSite> overlap = Sets.intersection(parentSites, childSites);
                if (!overlap.isEmpty()) {
                    selectedSite = overlap.iterator().next();
                }
            }

            if (selectedSite == null) {
                // TODO
            }


            return null;
        }

        @Override
        public PlacedCmdOp visit(CmdOpGroup op) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlacedCmdOp visit(CmdOpVar op) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlacedCmdOp visit(CmdOpToArg op) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    // Get the entry point for an exec site.
    // private Map<ExecSite, Object> someExecSiteInfo;

    // private FileMapper fileMapper;

    public PlacedCmdOp process(PlacedCommand pc) {
        Set<ExecSite> execSites = pc.execSites();
        CmdOpVisitor<PlacedCmdOp> placer = new CmdOpVisitor<>() {
            @Override
            public PlacedCmdOp visit(CmdOpExec op) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PlacedCmdOp visit(CmdOpPipeline op) {
                List<CmdOp> subOps = op.getSubOps();

            }

            @Override
            public PlacedCmdOp visit(CmdOpGroup op) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PlacedCmdOp visit(CmdOpVar op) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PlacedCmdOp visit(CmdOpToArg op) {
                // TODO Auto-generated method stub
                return null;
            }

        };


    }

    @Override
    public Stage visit(CmdOpExec op) {
        Stages.doc



        return null;
    }

    protected ExecSite requireExecSite(CmdOp op) {
        ExecSite result = opToExecSite.get(op);
        if (result == null) {
            throw new IllegalStateException("No exec site mapping entry for " + op);
        }
        return result;
    }

    @Override
    public Stage visit(CmdOpPipeline op) {
        ExecSite execSite = requireExecSite(op);

        List<CmdOp> subOps = op.getSubOps();
        for (CmdOp subOp : subOps) {
            ExecSite subExecSite = requireExecSite(subOp);
            if (subExecSite.equals(execSite)) {

            } else {

            }
        }

        // TODO Auto-generated method stub
        return null;
    }
}
