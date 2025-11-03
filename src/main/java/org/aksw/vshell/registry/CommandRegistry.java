package org.aksw.vshell.registry;

import java.util.Optional;
import java.util.Set;

import org.aksw.shellgebra.exec.model.ExecSite;

import com.google.common.collect.Multimap;

public interface CommandRegistry {
    /**
     * Return all known locations for a command.
     * Candidate locations where the command is unavailable
     * (e.g. on the host) must have been filtered out.
     */
    Multimap<ExecSite, String> get(String virtualCommandName);

    /** Find the best matching command for the given exec site. */
    default Optional<Set<String>> get(String virtualCommandName, ExecSite execSite) {
        return Optional.ofNullable((Set<String>)get(virtualCommandName).asMap().get(execSite));
    }

    /** Convenience method to get only a command's set of exec sites. */
    default Set<ExecSite> getExecSites(String virtualCommandName) {
        return get(virtualCommandName).keySet();
    }
}
