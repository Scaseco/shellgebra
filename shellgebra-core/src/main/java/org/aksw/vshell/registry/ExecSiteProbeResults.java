package org.aksw.vshell.registry;

import java.time.Instant;
import java.util.Optional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.aksw.commons.util.docker.Argv;
import org.aksw.shellgebra.exec.model.ExecSite;

/**
 * This structure tracks on which exec sites a command is
 * known to be un/available.
 */
public class ExecSiteProbeResults {

    public static ExecSiteProbeResults INSTANCE = null;

    /** Global in-memory instance. */
    public static ExecSiteProbeResults get() {
        if (INSTANCE == null) {
            synchronized (ExecSiteProbeResults.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ExecSiteProbeResults();
                }
            }
        }
        return INSTANCE;
    }

    // value may be null to indicate absence of the command.
    private Table<String, ExecSite, Availability> toolToExecSite = HashBasedTable.create();

    // Table for whether certain argv invocations are known to work.
    // Primary use case is tracking of entry point probe results.
    private Table<Argv, ExecSite, Availability> argvToExecSite = HashBasedTable.create();

    public ExecSiteProbeResults put(String command, ExecSite execSite, boolean isPresent) {
        Availability availability = new Availability(isPresent, null, Instant.now());
        toolToExecSite.put(command, execSite, availability);
        return this;
    }

    public Boolean get(String command, ExecSite execSite) {
        Availability availability = toolToExecSite.get(command, execSite);
        return availability == null ? null : availability.available();
    }

    public ExecSiteProbeResults put(Argv argv, ExecSite execSite, boolean isPresent) {
        Availability availability = new Availability(isPresent, null, Instant.now());
        argvToExecSite.put(argv, execSite, availability);
        return this;
    }

    public Optional<Availability> get(Argv argv, ExecSite execSite) {
        Availability availability = toolToExecSite.get(argv, execSite);
        return Optional.ofNullable(availability);
    }

    public boolean isKnownUnavailable(Argv argv, ExecSite execSite) {
        Optional<Availability> value = get(argv, execSite);
        boolean result = !value.map(Availability::available).orElse(true);
        return result;
    }
}
