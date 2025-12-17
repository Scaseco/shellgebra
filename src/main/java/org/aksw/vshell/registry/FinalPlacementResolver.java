package org.aksw.vshell.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOps;
import org.aksw.shellgebra.algebra.cmd.op.placed.PlacedCmdOp.PlacedCmd;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransform;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformBase;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.vshell.shim.rdfconvert.ArgumentList;

public class FinalPlacementResolver {
    public static FinalPlacement resolve(FinalPlacement inPlacement, ExecSiteResolver resolver, CommandCatalog inferredCatalog) {
        PlacedCmd root = inPlacement.cmdOp();

        Map<CmdOpVar, PlacedCmd> inMap = inPlacement.placements();
        FinalPlacementResolverWorker worker = new FinalPlacementResolverWorker(resolver, inferredCatalog, inMap);

        CmdOp cmdOp = root.cmdOp();
        ExecSite execSite = root.execSite();

        PlacedCmd tmp = worker.place(cmdOp, execSite);
        Map<CmdOpVar, PlacedCmd> outMap = worker.getOutMap();
        return new FinalPlacement(tmp, outMap);
    }

    public static CmdOp resolve(CmdOp cmdOp, ExecSite execSite, ExecSiteResolver resolver, CommandCatalog inferredCatalog) {
        CmdOpTransform transform = new CmdOpTransformBase() {
            @Override
            public CmdOp transform(CmdOpExec op, List<CmdArg> subOps) {
                String inName = op.name();
                boolean doResolve = false;
                String outName;
                if (doResolve) {
                    outName = inferredCatalog.get(inName, execSite).map(s -> s.iterator().next()).orElse(null);
                    if (outName == null) {
                        outName = resolver.resolve(inName, execSite)
                            .orElseThrow(() -> {
                                return new RuntimeException("Should not happen - could not resolve: " + inName + " on site " + execSite);
                            });
                    }
                } else {
                    outName = inName;
                }
                return new CmdOpExec(op.prefixes(), outName, ArgumentList.of(subOps));
            }
        };

        CmdOp result = CmdOpTransformer.transform(cmdOp, transform, null, null);
        return result;
    }

    static class FinalPlacementResolverWorker {
        private Map<CmdOpVar, PlacedCmd> inMap;
        private Map<CmdOpVar, PlacedCmd> outMap = new HashMap<>();
        private ExecSiteResolver resolver;
        private CommandCatalog inferredCatalog;

        public FinalPlacementResolverWorker(ExecSiteResolver resolver, CommandCatalog inferredCatalog, Map<CmdOpVar, PlacedCmd> inMap) {
            super();
            this.resolver = resolver;
            this.inferredCatalog = inferredCatalog;
            this.inMap = inMap;
        }

        public Map<CmdOpVar, PlacedCmd> getOutMap() {
            return outMap;
        }

        public PlacedCmd place(CmdOp inParentOp, ExecSite parentSite) {
            CmdOp outParentOp = resolve(inParentOp, parentSite, resolver, inferredCatalog);

            Set<CmdOpVar> parentVars = CmdOps.accVars(inParentOp);

            // Inline variables that use the same exec site
            // CmdOp outParentOp = inParentOp;
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

//            if (!substMap.isEmpty()) {
//                outParentOp = CmdOps.subst(outParentOp, substMap::get);
//            }

            return new PlacedCmd(outParentOp, parentSite);
        }
    }
}
