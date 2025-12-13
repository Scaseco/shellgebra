package org.aksw.vshell.registry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.aksw.shellgebra.exec.model.ExecSite;

/**
 * This structure tracks on which exec sites a command is
 * known to be un/available.
 */
public class CommandAvailability {

    public static CommandAvailability INSTANCE = null;

    /** Global in-memory instance. */
    public static CommandAvailability get() {
        if (INSTANCE == null) {
            synchronized (CommandAvailability.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CommandAvailability();
                }
            }
        }
        return INSTANCE;
    }

    // value may be null to indicate absence of the command.
    private Table<String, ExecSite, Boolean> toolToExecSite = HashBasedTable.create();

    public CommandAvailability put(String command, ExecSite execSite, Boolean isPresent) {
        toolToExecSite.put(command, execSite, isPresent);
        return this;
    }

    public Boolean get(String command, ExecSite execSite) {
        return toolToExecSite.get(command, execSite);
    }
}
