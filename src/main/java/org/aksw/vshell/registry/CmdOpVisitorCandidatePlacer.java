package org.aksw.vshell.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpGroup;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpPipeline;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.exec.model.ExecSite;
import org.aksw.shellgebra.exec.model.ExecSites;
import org.aksw.shellgebra.exec.model.PlacedCommand;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;


public class CmdOpVisitorCandidatePlacer
    implements CmdOpVisitor<PlacedCommand>
{
    /** ExecSiteResolver can test exec sites for whether they provide a command. */
    // private CommandReg
    private CommandRegistry cmdRegistry;
    private ExecSiteResolver execSiteResolver;
    private Set<ExecSite> preferredExecSites;
    private Map<CmdOp, Set<ExecSite>> opToSites = new IdentityHashMap<>();
    private Map<CmdOpVar, PlacedCommand> varToPlacement = new HashMap<>();

    private int nextVar = 0;

    public CmdOpVisitorCandidatePlacer(CommandRegistry cmdRegistry, ExecSiteResolver execSiteResolver, Set<ExecSite> preferredExecSites) {
        super();
        this.cmdRegistry = cmdRegistry;
        this.execSiteResolver = execSiteResolver;
        this.preferredExecSites = preferredExecSites;
    }

    public Map<CmdOpVar, PlacedCommand> getVarToPlacement() {
        return varToPlacement;
    }

    @Override
    public PlacedCommand visit(CmdOpExec op) {
        // TODO Introspect docker images for whether the entry point
        // needs to be set to a shell in order to run a command.

        // Allocate the op's to-be-filled set of exec sites.
        Set<ExecSite> execSites = opToSites.computeIfAbsent(op, k -> new HashSet<>());
        String virtCmdName = op.getName();

        // Find the set of physical commands for the virtual one and see if it exists
        // in the image.
        // TODO: In general a validator is needed to confirm that an existing command
        // is actually suitable.
        Multimap<ExecSite, String> candResolutions = cmdRegistry.get(virtCmdName); // execSiteResolver.resolve(virtCmdName);
        Set<String> candCmdLocations = new LinkedHashSet<>(candResolutions.values());

        // Check all preferred exec sites for whether they provide the command.
        for (ExecSite execSite : preferredExecSites) {
            for (String cmdLocation : candCmdLocations) {
                boolean isCmdPresent = execSiteResolver.providesCommand(cmdLocation, execSite);
                if (isCmdPresent) {
                    execSites.add(execSite);
                }
            }
        }
        System.out.println("Placement: " + op + ": " + execSites);

        // If no exec sites were found in the preferred exec sites then
        // try to resolve.
        // TODO the execSiteResolver actually internally uses the same candResolutions map
        //      but we don't know that and may thus try the same candidate again.
        //      Perhaps the candResolutions should be an argument to the resolver?
        // NOTE The availability cache should still hit for double checks.
        // TODO Apply the cmd availability cache to the entry point lookup!
        if (execSites.isEmpty()) {
            Map<ExecSite, String> resolutions = execSiteResolver.resolve(virtCmdName);
            execSites.addAll(resolutions.keySet());
        }

        // Whether to check all sub-op images (based on arguments of the command)
        // for whether it contains the command.
        boolean checkPresenceInImages = true;

        // Check which docker images provide the command.
        opToSites.put(op, execSites);
        return new PlacedCommand(op, execSites);
    }

    @Override
    public PlacedCommand visit(CmdOpPipeline op) {
         List<CmdOp> subOps = op.subOps();
         PlacedCommand result = process(subOps, CmdOpPipeline::new);
         return result;
    }


    @Override
    public PlacedCommand visit(CmdOpGroup op) {
        List<CmdOp> subOps = op.subOps();
        PlacedCommand result = process(subOps, CmdOpGroup::new);
        return result;
    }

    @Override
    public PlacedCommand visit(CmdOpVar op) {
        throw new RuntimeException("var resolution not supported.");
    }

    protected PlacedCommand process(List<CmdOp> subOps, Function<List<CmdOp>, CmdOp> ctor) {
        // Check whether this operation can be handled on the preferred sites
        // - they need to provide a bash
        // If not then check which children can do the op
        // if there still are none then try a default image (e.g. ubuntu)
        List<PlacedCommand> placements = new ArrayList<>(subOps.size());
        for (CmdOp subOp : subOps) {
            PlacedCommand contrib = subOp.accept(this);
            // Sanity checks - there must be a placement (or bailout to a prior exception - such as command not found).
            if (contrib == null) {
                throw new RuntimeException("Null returned as placement for: " + subOp);
            }
            if (contrib.execSites().isEmpty()) {
                throw new IllegalStateException("Empty set of exec sites returned for: " + subOp);
            }
            placements.add(contrib);
        }

        // Simple approach: Iterate - as long as the intersection is non empty add.
        // If it becomes empty, then pick one candidate from the set that was not empty.
        Set<ExecSite> currentSet = null;
        Set<ExecSite> nextSet;
        List<PlacedCommand> childStreaks = new ArrayList<>(subOps.size());

        int currentStreakOffset = 0;
        int n = placements.size();
        int i;
        for (i = 0; i < n; ++i) {
            PlacedCommand pc = placements.get(i);
            Set<ExecSite> rawContrib = pc.execSites();

            // If there is a previous execSite then
            // remove execSites that cannot run a pipeline
            Set<ExecSite> compatContrib = rawContrib;
            if (currentSet != null) {
                compatContrib = new LinkedHashSet<>();
                for (ExecSite c : rawContrib) {
                    boolean canRunPipeline = execSiteResolver.canRunPipeline(c);
                    if (canRunPipeline) {
                        compatContrib.add(c);
                    }
                }
            }

            nextSet = (currentSet == null)
                ? rawContrib
                : Sets.intersection(compatContrib, currentSet);

            if (nextSet.isEmpty()) {
                // Need to place the current streak.
                List<PlacedCommand> currentStreak = placements.subList(currentStreakOffset, i);
                PlacedCommand placed = doPlacement(ctor, preferredExecSites, currentSet, currentStreak);
                childStreaks.add(placed);
                currentSet = rawContrib;
                currentStreakOffset = i;
            } else {
                currentSet = nextSet;
                // Note: We have checked that there are exec sites in the intersection
                // that can run a pipeline
                continue;
            }
        }

        // Place the remainder (if any).
        if (currentSet != null) {
            List<PlacedCommand> currentStreak = placements.subList(currentStreakOffset, i);
            PlacedCommand placed = doPlacement(ctor, preferredExecSites, currentSet, currentStreak);
            childStreaks.add(placed);
        }


        PlacedCommand result;
        if (childStreaks.size() == 1) {
            result = childStreaks.get(0);
        } else {
            // Select some pipeline that can run a pipeline
            Set<ExecSite> parentExecSites = new LinkedHashSet<>();

            Set<ExecSite> childExecSites = childStreaks.stream()
                    .map(PlacedCommand::execSites)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (ExecSite childExecSite : childExecSites) {
                if (execSiteResolver.canRunPipeline(childExecSite)) {
                    parentExecSites.add(childExecSite);
                    break;
                }
            }

            if (parentExecSites.isEmpty()) {
                // TODO Verify that it can run pipelines!
                parentExecSites.add(ExecSites.host());
            }

            // Assemble the parent pipeline.
            List<CmdOp> newSubOps = new ArrayList<>(childStreaks.size());
            for (PlacedCommand childStreak : childStreaks) {
                // ExecSite childExecSite = childStreak.getExecSite();
//                if (parentExecSite.equals(childExecSite)) {
//                    newSubOps.add(childStreak.cmdOp());
//                } else {
                    // CmdOpVar newVar = alloc
                    CmdOpVar cmdOpVar = new CmdOpVar("v" + (nextVar++));
                    newSubOps.add(cmdOpVar);
                    varToPlacement.put(cmdOpVar, childStreak);
//                }
            }

            CmdOp newCmdOp = ctor.apply(newSubOps);
            result = new PlacedCommand(newCmdOp, parentExecSites);
            // TODO Do we need a placed pipeline? We could put the whole pipeline into a cmd and place it...
            // The main question is whether we want to have separate phases for candidate exec site selection
            // and stage generation...

            // TODO Find an exec site that can run the pipeline.
//            ExecSite execSite = ExecSites.host();
//            result = new PlacedPipeline(childStreaks, execSite);
        }
        return result;

        // return result;

        // TODO Should we count how often an image is used in a subtree?
        //      we could use that to schedule larger expressions to the same image.
        //      actually, in the placed cmd we have the sub expression - so we can just count the number of nodes in it.
    }

//    protected getExecSitesChecked() {
//
//    }

    protected PlacedCommand doPlacement(Function<List<CmdOp>, CmdOp> ctor, Set<ExecSite> preferredExecSites, Set<ExecSite> candidateExecSites, List<PlacedCommand> streak) {
        if (candidateExecSites.isEmpty()) {
            throw new IllegalArgumentException("Candidate exec site set must not be empty.");
        }
        Set<ExecSite> effectiveExecSites = Sets.intersection(preferredExecSites, candidateExecSites);
        if (effectiveExecSites.isEmpty()) {
            effectiveExecSites = candidateExecSites;
        }

        // ExecSite selectedExecSite = effectiveExecSites.iterator().next();
        List<CmdOp> placedSubOps = streak.stream().map(PlacedCommand::cmdOp).toList();

        // <() / =()
        CmdOp part = ctor.apply(placedSubOps);
        // PlacedCmd placed = new PlacedCmd(partPipeline, selectedExecSite);
        PlacedCommand placed = new PlacedCommand(part, effectiveExecSites);
        // varToPlacement.put(cmdOpVar, new PlacedCmd(partPipeline, selectedExecSite));

        return placed;
    }
}
