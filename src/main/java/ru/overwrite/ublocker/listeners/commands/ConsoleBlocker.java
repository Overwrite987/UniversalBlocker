package ru.overwrite.ublocker.listeners.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.server.ServerCommandEvent;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.utils.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

public class ConsoleBlocker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;

    public ConsoleBlocker(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent e) {
        String command = e.getCommand().toLowerCase();
        for (CommandGroup group : pluginConfig.commandBlockGroupSet) {
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

    private void checkStringBlock(ServerCommandEvent e, String command, CommandGroup group) {
        for (String com : group.getCommandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
            if (executedCommandBase.equalsIgnoreCase(com) || aliases.contains(executedCommandBase.replace("/", ""))) {
                List<Action> actions = group.getActionsToExecute();
                if (actions.isEmpty()) {
                    continue;
                }
                if (shouldBlockCommand(group, com, command, aliases, actions)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(ServerCommandEvent e, String command, CommandGroup group) {
        for (Pattern pattern : group.getCommandsToBlockPattern()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(Utils.cutCommand(command).replace("/", ""));
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (aliases.contains(matcher.group())) {
                    if (shouldBlockCommand(group, matcher.group(), command, aliases, actions)) {
                        e.setCancelled(true);
                        break;
                    }
                }
                if (shouldBlockCommand(group, matcher.group(), command, aliases, actions)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldBlockCommand(CommandGroup group, String com, String command, List<String> aliases, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK_CONSOLE: {
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (com.equalsIgnoreCase(alias)) {
                                return true;
                            }
                        }
                    }
                    return com.equals(executedCommandBase);
                }
                case LOG: {
                    String[] coAction = action.context().split("file=");
                    plugin.logAction(coAction[0], coAction[1]);
                    break;
                }
                default:
                    break;
            }
        }
        return false;
    }
}
