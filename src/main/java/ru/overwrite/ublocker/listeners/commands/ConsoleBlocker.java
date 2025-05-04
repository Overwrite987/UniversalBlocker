package ru.overwrite.ublocker.listeners.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public static boolean FULL_LOCK;

    public ConsoleBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent e) {
        if (FULL_LOCK) {
            e.setCancelled(true);
            return;
        }
        String command = e.getCommand().toLowerCase();
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_COMMANDS);
            Utils.printDebug("Block type: " + group.blockType(), Utils.DEBUG_COMMANDS);
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            switch (group.blockType()) {
                case STRING: {
                    checkStringBlock(e, command, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, command, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(ServerCommandEvent e, String command, CommandGroup group) {
        for (String com : group.commandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
            if (executedCommandBase.equalsIgnoreCase(com) || aliases.contains(executedCommandBase.substring(1))) {
                List<Action> actions = group.actionsToExecute();
                if (shouldBlockCommand(e, actions)) {
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(ServerCommandEvent e, String command, CommandGroup group) {
        for (Pattern pattern : group.commandsToBlockPattern()) {
            Matcher matcher = pattern.matcher(Utils.cutCommand(command).replace("/", ""));
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                List<Action> actions = group.actionsToExecute();
                if (aliases.contains(matcher.group())) {
                    if (shouldBlockCommand(e, actions)) {
                        break;
                    }
                }
                if (shouldBlockCommand(e, actions)) {
                    break;
                }
            }
        }
    }

    private boolean shouldBlockCommand(Cancellable e, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK_CONSOLE: {
                    e.setCancelled(true);
                }
                case LOG: {
                    if (!e.isCancelled())
                        break;
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
                    String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
                    plugin.logAction(logMessage, file);
                    break;
                }
                default:
                    break;
            }
        }
        return e.isCancelled();
    }
}
