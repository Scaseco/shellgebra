package org.aksw.vshell.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.exec.model.ExecSite;

public class FinalPlacementInliner {

    public static FinalPlacement inline(FinalPlacement inPlacement) {
        PlacedCmd root = inPlacement.cmdOp();
        Map<CmdOpVar, PlacedCmd> inMap = inPlacement.placements();
        Worker worker = new Worker(inMap);

        CmdOp cmdOp = root.cmdOp();
        ExecSite execSite = root.execSite();

        PlacedCmd tmp = worker.place(cmdOp, execSite);
        Map<CmdOpVar, PlacedCmd> outMap = worker.getOutMap();
        return new FinalPlacement(tmp, outMap);
    }

    static class Worker {
        private Map<CmdOpVar, PlacedCmd> inMap;
        private Map<CmdOpVar, PlacedCmd> outMap = new HashMap<>();

        public Worker(Map<CmdOpVar, PlacedCmd> inMap) {
            super();
            this.inMap = inMap;
        }

        public Map<CmdOpVar, PlacedCmd> getOutMap() {
            return outMap;
        }

        public PlacedCmd place(CmdOp inParentOp, ExecSite parentSite) {
            Set<CmdOpVar> parentVars = CmdOps.accVars(inParentOp);

            // Inline variables that use the same exec site
            CmdOp outParentOp = inParentOp;
            // Map<CmdOpVar, CmdOp> outMap = new HashMap<>();
            Map<CmdOpVar, CmdOp> substMap = new HashMap<>();
            for (CmdOpVar v : parentVars) {
                PlacedCmd inChild = inMap.get(v);
                CmdOp inChildCmdOp = inChild.cmdOp();
                ExecSite inChildExecSite = inChild.execSite();

                PlacedCmd outChild = place(inChildCmdOp, inChildExecSite);
                CmdOp outChildOp = outChild.cmdOp();
                ExecSite outChildExecSite = outChild.execSite();

                if (outChildExecSite.equals(parentSite)) {
                    substMap.put(v, outChildOp);
                } else {
                    PlacedCmd childPlacement = place(outChildOp, outChildExecSite);
                    outMap.put(v, childPlacement);
                }
            }

            if (!substMap.isEmpty()) {
                outParentOp = CmdOps.subst(outParentOp, substMap::get);
            }

            return new PlacedCmd(outParentOp, parentSite);
        }
    }
}
