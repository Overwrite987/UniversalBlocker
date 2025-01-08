package ru.overwrite.ublocker.listeners.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RconBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public RconBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onRconCommand(RemoteServerCommandEvent e) {
        String command = e.getCommand().toLowerCase();
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            if (Utils.DEBUG) {
                plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
                plugin.getPluginLogger().info("Block type: " + group.getBlockType());
            }
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            switch (group.getBlockType()) {
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

    private void checkStringBlock(RemoteServerCommandEvent e, String command, CommandGroup group) {
        for (String com : group.getCommandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
            if (executedCommandBase.equalsIgnoreCase(com) || aliases.contains(executedCommandBase.substring(1))) {
                List<Action> actions = group.getActionsToExecute();
                if (shouldBlockCommand(e, actions)) {
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(RemoteServerCommandEvent e, String command, CommandGroup group) {
        for (Pattern pattern : group.getCommandsToBlockPattern()) {
            Matcher matcher = pattern.matcher(Utils.cutCommand(command).replace("/", ""));
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                List<Action> actions = group.getActionsToExecute();
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
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER, true);
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
