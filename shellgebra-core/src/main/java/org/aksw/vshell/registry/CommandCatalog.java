package org.aksw.vshell.registry;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Multimap;

import org.aksw.shellgebra.exec.model.ExecSite;

// TODO: Instead of just the physical command name, we generally need an ArgumentChecker
//       that can analyze the physical command and extract the supported arguments from it.
//       in the simple case, the argument provider is a static model.
//       in the complex case, we need to start an environment and probe the command in it.
public interface CommandCatalog {
    /**
     * Return all known locations for a command.
     * Candidate locations where the command is unavailable
     * (e.g. on the host) must have been filtered out.
     */
    Multimap<ExecSite, CommandBinding> get(String virtualCommandName);

    /** Find the best matching command for the given exec site. */
    default Optional<Set<CommandBinding>> get(String virtualCommandName, ExecSite execSite) {
        return Optional.ofNullable((Set<CommandBinding>)get(virtualCommandName).asMap().get(execSite));
    }

    /** Convenience method to get only a command's set of exec sites. */
    default Set<ExecSite> getExecSites(String virtualCommandName) {
        return get(virtualCommandName).keySet();
    }
}
