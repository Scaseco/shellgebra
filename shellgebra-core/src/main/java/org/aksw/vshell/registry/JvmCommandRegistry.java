package org.aksw.vshell.registry;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class JvmCommandRegistry {
    private Map<String, JvmCommand> map = new ConcurrentHashMap<>();

    private static JvmCommandRegistry INSTANCE = null;

    /** Get-or-create the singleton instance. */
    public static JvmCommandRegistry get() {
        if (INSTANCE == null) {
            synchronized (JvmCommandRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JvmCommandRegistry();
                }
            }
        }
        return INSTANCE;
    }

    public JvmCommandRegistry put(String commandName, JvmCommand cmd) {
        map.put(commandName, cmd);
        return this;
    }

    public Optional<JvmCommand> get(String commandName) {
        return Optional.ofNullable(map.get(commandName));
    }

    public JvmCommand require(String commandName) {
        JvmCommand result = get(commandName)
            .orElseThrow(() -> new NoSuchElementException("Command not found: " + commandName));
        return result;
    }

    /** Shorthand to lookup a command a build a stage for it. */
//    public Stage newStage(String commandName, String ...args) {
//        JvmCommand cmd = require(commandName);
//        Stage result = cmd.newStage(args);
//        return result;
//    }
}
