package org.aksw.vshell.registry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.shellgebra.exec.model.ExecSite;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Registry / cache for whether a command is available on a certain exec site.
 * Should only be used with resolved commands such as /bin/foo
 * in contrast to names such as foo.
 */
public class CommandRegistryImpl
    implements CommandRegistry
{
    // value may be null to indicate absence of the command.
    private Table<String, ExecSite, String> toolToSiteToCmd = HashBasedTable.create();

    /** Return a snapshot of the set of exec sites where the command is
     *  known to be present.
     *  Return null if there is no entry for the command.
     *  Empty map if there is an entry with no known locations.
     */
    public Optional<Map<ExecSite, String>> getKnownExecSites(String command) {
        Map<ExecSite, String> tmp = null;
        if (toolToSiteToCmd.containsRow(command)) {
            tmp = toolToSiteToCmd.row(command).entrySet().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
        }
        return Optional.ofNullable(tmp);
    }

//    public Optional<Set<CommandLocation>> getKnownLocations(String command) {
//        return getKnownExecSites(command).map(m -> m.entrySet().stream()
//            .map(e -> new CommandLocation(e.getValue(), e.getKey()))
//            .collect(Collectors.toUnmodifiableSet()));
//    }

    public Optional<String> getAvailability(String command, ExecSite execSite) {
        return Optional.ofNullable(toolToSiteToCmd.get(command, execSite));
    }

    public CommandRegistryImpl put(String command, ExecSite execSite, String cmd) {
        toolToSiteToCmd.put(command, execSite, cmd);
        return this;
    }

    public CommandRegistryImpl putAll(String command, Map<ExecSite, String> cmdMap) {
        toolToSiteToCmd.row(command).putAll(cmdMap);
        return this;
    }

    @Override
    public Map<ExecSite, String> get(String virtualCommandName) {
        return getKnownExecSites(virtualCommandName).orElse(Map.of());
    }
}
