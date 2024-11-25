package ru.overwrite.ublocker.listeners.commands;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.configuration.Config;

public class TabComplete implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public TabComplete(UniversalBlocker plugin) {
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
                ObjectList<Action> actions = group.getActionsToExecute();
                if (actions.isEmpty()) {
                    continue;
                }
                Command comInMap = Bukkit.getCommandMap().getCommand(buffer);
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(group, p, buffer, buffer, aliases, actions) || aliases.contains(buffer)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(AsyncTabCompleteEvent e, Player p, String buffer, CommandGroup group) {
        for (Pattern pattern : group.getCommandsToBlockPattern()) {
            ObjectList<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(buffer.split(" ")[0]);
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(group, p, matcher.group(), buffer, aliases, actions) || aliases.contains(matcher.group())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldBlockTabComplete(CommandGroup group, Player p, String com, String command, List<String> aliases, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK_TAB_COMPLETE: {
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
                case LITE_BLOCK_TAB_COMPLETE: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), "perm={", "}"),
                            "ublocker.bypass.commands");
                    if (p.hasPermission(perm)) {
                        return false;
                    }
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (Bukkit.getCommandMap().getCommand(com).getAliases().contains(alias)) {
                                return true;
                            }
                        }
                    }
                    return com.equals(executedCommandBase);
                }
                default:
                    break;
            }
        }
        return false;
    }
}
