package org.aksw.vshell.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.PlacedCommand;

public class CandidatePlacer {
    private CommandRegistry cmdRegistry;
    private ExecSiteResolver execSiteResolver;
    private Set<ExecSite> preferredExecSites;
    private Map<CmdOp, Set<ExecSite>> opToSites = new IdentityHashMap<>();
    private Map<CmdOpVar, PlacedCommand> varToPlacement = new HashMap<>();

    public CandidatePlacer(CommandRegistry cmdRegistry, ExecSiteResolver execSiteResolver, Set<ExecSite> preferredExecSites) {
        super();
        this.cmdRegistry = cmdRegistry;
        this.execSiteResolver = execSiteResolver;
        this.preferredExecSites = preferredExecSites;
    }

    // public CandidatePlacement place()

}
