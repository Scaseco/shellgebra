package org.aksw.vshell.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.PlacedCommand;

public class FinalPlacer {

    public static FinalPlacement place(CandidatePlacement candPlacement) {
        PlacedCommand root = candPlacement.root();
        CmdOp cmdOp = root.cmdOp();
        Set<ExecSite> execSites = root.execSites();

        Worker worker = new Worker(candPlacement.placements());

        PlacedCmd tmp = worker.place(cmdOp, execSites);
        Map<CmdOpVar, PlacedCmd> outMap = worker.getOutMap();
        return new FinalPlacement(tmp, outMap);
    }

    static class Worker {
        private Map<CmdOpVar, PlacedCommand> inMap;
        private Map<CmdOpVar, PlacedCmd> outMap = new HashMap<>();

        public Worker(Map<CmdOpVar, PlacedCommand> inMap) {
            super();
            this.inMap = inMap;
        }

        public Map<CmdOpVar, PlacedCmd> getOutMap() {
            return outMap;
        }

        public PlacedCmd place(CmdOp parentOp, Set<ExecSite> parentSites) {
            Set<CmdOpVar> parentVars = CmdOps.accVars(parentOp);
            ExecSite pick = parentSites.iterator().next();

            // Inline variables that use the same exec site
            for (CmdOpVar v : parentVars) {
                PlacedCommand child = inMap.get(v);
                CmdOp childCmdOp = child.cmdOp();
                Set<ExecSite> childSites = child.execSites();
                if (childSites.contains(pick)) {
                    outMap.put(v, new PlacedCmd(childCmdOp, pick));
                } else {
                    PlacedCmd childPlacement = place(childCmdOp, childSites);
                    outMap.put(v, childPlacement);
                }
            }
            return new PlacedCmd(parentOp, pick);
        }
    }
}
