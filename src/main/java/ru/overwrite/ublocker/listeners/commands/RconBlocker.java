package ru.overwrite.ublocker.listeners.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RconBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public static boolean FULL_LOCK;

    public RconBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onRconCommand(RemoteServerCommandEvent e) {
        if (FULL_LOCK) {
            e.setCancelled(true);
            return;
        }
        String command = e.getCommand().toLowerCase();
        outer:
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_COMMANDS);
            Utils.printDebug("Block type: " + group.blockType(), Utils.DEBUG_COMMANDS);
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

    private boolean checkStringBlock(RemoteServerCommandEvent e, String command, CommandGroup group) {
        for (String com : group.commandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = Utils.cutCommand(command).toLowerCase();
            String baseCommand = !executedCommandBase.isEmpty() && executedCommandBase.charAt(0) == '/'
                    ? executedCommandBase.substring(1)
                    : executedCommandBase;

            if (com.equalsIgnoreCase(baseCommand) || aliases.contains(baseCommand)) {
                List<Action> actions = group.actionsToExecute();
                if (executeActions(e, command, baseCommand, actions)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPatternBlock(RemoteServerCommandEvent e, String command, CommandGroup group) {
        for (Pattern pattern : group.commandsToBlockPattern()) {
            String baseCommand = Utils.cutCommand(command).replace("/", "");
            Matcher matcher = pattern.matcher(baseCommand);
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (aliases.contains(matcher.group())) {
                    List<Action> actions = group.actionsToExecute();
                    if (executeActions(e, command, matcher.group(), actions)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private final String[] searchList = {"%player%", "%command%", "%msg%"};

    public boolean executeActions(Cancellable e, String fullCommand, String baseCommand, List<Action> actions) {
        Utils.printDebug("Starting executing actions for rcon and blocked command '" + baseCommand + "'", Utils.DEBUG_COMMANDS);
        final String[] replacementList = {"RCON", baseCommand, fullCommand};

        for (Action action : actions) {
            ActionType type = action.type();

            if (type == ActionType.BLOCK_RCON) {
                Utils.printDebug("Command event blocked for rcon", Utils.DEBUG_COMMANDS);
                e.setCancelled(true);
            }

            if (e.isCancelled()) {
                switch (type) {
                    case LOG: {
                        logAction(action, replacementList);
                        break;
                    }
                    case NOTIFY_CONSOLE: {
                        sendNotifyConsole(action, replacementList);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return e.isCancelled();
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
}
