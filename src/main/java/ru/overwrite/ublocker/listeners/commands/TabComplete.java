package ru.overwrite.ublocker.listeners.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.utils.Config;

public class TabComplete implements Listener {

    private final Main plugin;
    private final Config pluginConfig;

    public TabComplete(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!(e.getSender() instanceof Player p)) {
            return;
        }
        if (plugin.isExcluded(p))
            return;
        String buffer = e.getBuffer();
        // Херня которая запрещает стилить плагины через читы, а за одно еще и предотвращает краш таб-комплитом
        if ((buffer.split(" ").length == 1 && !buffer.endsWith(" ")) || !buffer.startsWith("/") || buffer.length() > 256) {
            e.setCancelled(true);
            return;
        }
        for (CommandGroup group : pluginConfig.commandBlockGroupSet) {
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, buffer, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, buffer, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(AsyncTabCompleteEvent e, Player p, String buffer, CommandGroup group) {
        for (String command : group.getCommandsToBlockString()) {
            if (buffer.equalsIgnoreCase(command + " ")) {
                List<Action> actions = group.getActionsToExecute();
                if (actions.isEmpty()) {
                    continue;
                }
                Command comInMap = Bukkit.getCommandMap().getCommand(buffer);
                List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
                if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(p, buffer, buffer, aliases, actions) || aliases.contains(buffer)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(AsyncTabCompleteEvent e, Player p, String buffer, CommandGroup group) {
        for (Pattern pattern : group.getCommandsToBlockPattern()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(buffer.split(" ")[0]);
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(p, matcher.group(), buffer, aliases, actions) || aliases.contains(matcher.group())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldBlockTabComplete(Player p, String com, String command, List<String> aliases, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK_TAB_COMPLETE: {
                    List<String> contextList = action.context().contains(",") ? List.of(action.context().split(",")) : List.of(action.context());
                    if (contextList.get(0).isBlank()) {
                        return true;
                    }
                    String executedCommandBase = command.contains(" ") ? command.split(" ")[0] : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        return true;
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (com.equalsIgnoreCase(s)) {
                                return true;
                            }
                        }
                    }
                    break;
                }
                case LITE_BLOCK_TAB_COMPLETE: {
                    String[] coAction = action.context().split("perm=");
                    List<String> contextList = coAction[0].contains(",") ? List.of(coAction[0].trim().split(",")) : List.of(coAction[0].trim());
                    if (contextList.isEmpty()) {
                        return true;
                    }
                    String executedCommandBase = command.contains(" ") ? command.split(" ")[0] : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        if (!p.hasPermission(coAction[1])) {
                            return true;
                        }
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (Bukkit.getCommandMap().getCommand(com).getAliases().contains(s) && !p.hasPermission(coAction[1])) {
                                return true;
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
        return false;
    }
}
