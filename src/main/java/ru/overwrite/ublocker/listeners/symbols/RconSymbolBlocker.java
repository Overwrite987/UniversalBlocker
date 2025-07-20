package ru.overwrite.ublocker.listeners.symbols;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RconSymbolBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public RconSymbolBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(RemoteServerCommandEvent e) {
        String command = e.getCommand().toLowerCase();
        outer:
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_SYMBOLS);
            if (group.blockFactor().isEmpty() || !group.blockFactor().contains("rcon_command")) {
                Utils.printDebug("Group " + group.groupId() + " does not have 'rcon_command' block factor. Skipping...", Utils.DEBUG_SYMBOLS);
                continue;
            }
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            switch (group.blockType()) {
                case STRING: {
                    if (checkStringBlock(e, command, group)) {
                        break outer;
                    }
                    break;
                }
                case PATTERN: {
                    if (checkPatternBlock(e, command, group)) {
                        break outer;
                    }
                    break;
                }
            }
        }
    }

    private boolean checkStringBlock(RemoteServerCommandEvent e, String command, SymbolGroup group) {
        for (String symbol : group.symbolsToBlock()) {
            if (startWithExcludedString(command, group.excludedCommandsString())) {
                continue;
            }
            if (command.contains(symbol)) {
                Utils.printDebug("Command '" + command + "' contains blocked symbol" + symbol + ". (String)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                executeActions(e, command, symbol, actions);
                return true;
            }
        }
        return false;
    }

    private boolean checkPatternBlock(RemoteServerCommandEvent e, String command, SymbolGroup group) {
        for (Pattern pattern : group.patternsToBlock()) {
            Matcher matcher = pattern.matcher(command);
            if (startWithExcludedPattern(command, group.excludedCommandsPattern())) {
                continue;
            }
            if (matcher.find()) {
                Utils.printDebug("Command '" + command + "' contains blocked symbol" + matcher.group() + ". (Pattern)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                executeActions(e, command, matcher.group(), actions);
                return true;
            }
        }
        return false;
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    public void executeActions(Cancellable e, String command, String symbol, List<Action> actions) {
        Utils.printDebug("Starting executing actions for rcon and blocked symbol '" + symbol + "' (COMMAND)", Utils.DEBUG_SYMBOLS);
        final String[] replacementList = {"RCON", symbol, command};

        for (Action action : actions) {
            ActionType type = action.type();

            if (shouldBlockAction(type)) {
                Utils.printDebug("Command event blocked for rcon", Utils.DEBUG_SYMBOLS);
                e.setCancelled(true);
                continue;
            }

            if (e.isCancelled()) {
                if (type == ActionType.LOG) {
                    logAction(action, replacementList);
                }
                if (type == ActionType.NOTIFY_CONSOLE) {
                    sendNotifyConsole(action, replacementList);
                }
            }
        }
    }

    private boolean shouldBlockAction(ActionType type) {
        return type == ActionType.BLOCK;
    }

    private void logAction(Action action, String[] replacementList) {
        String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
        String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
        plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
    }

    private void sendNotifyConsole(Action action, String[] replacementList) {
        String formattedMessage = formatActionMessage(action, replacementList);
        Bukkit.getConsoleSender().sendMessage(formattedMessage);
    }

    private String formatActionMessage(Action action, String[] replacementList) {
        return Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
    }

    private boolean startWithExcludedString(String command, List<String> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (String excluded : excludedList) {
            if (command.startsWith(excluded + " ")) {
                return true;
            }
        }
        return false;
    }

    private boolean startWithExcludedPattern(String command, List<Pattern> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (Pattern excluded : excludedList) {
            Matcher matcher = excluded.matcher(command);
            if (matcher.lookingAt()) {
                return true;
            }
        }
        return false;
    }
}
