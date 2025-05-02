package ru.overwrite.ublocker.listeners.commands;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if (Utils.DEBUG) {
                plugin.getPluginLogger().warn("Preventing illegal tab complete action from player " + p.getName());
                plugin.getPluginLogger().warn("Tab complete buffer: " + buffer);
            }
            e.setCancelled(true);
            return;
        }
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId());
            Utils.printDebug("Block type: " + group.blockType());
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.conditionsToCheck())) {
                Utils.printDebug("Blocking does not fulfill the requirements. Skipping group...");
                continue;
            }
            switch (group.blockType()) {
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
        for (String command : group.commandsToBlockString()) {
            if (buffer.equalsIgnoreCase(command + " ")) {
                List<Action> actions = group.actionsToExecute();
                Command comInMap = Bukkit.getCommandMap().getCommand(buffer);
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(p, actions) || aliases.contains(buffer)) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private void checkPatternBlock(AsyncTabCompleteEvent e, Player p, String buffer, CommandGroup group) {
        for (Pattern pattern : group.commandsToBlockPattern()) {
            List<Action> actions = group.actionsToExecute();
            Matcher matcher = pattern.matcher(buffer.split(" ")[0]);
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (shouldBlockTabComplete(p, actions) || aliases.contains(matcher.group())) {
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean shouldBlockTabComplete(Player p, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK_TAB_COMPLETE: {
                    return true;
                }
                case LITE_BLOCK_TAB_COMPLETE: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                            "ublocker.bypass.commands");
                    return !p.hasPermission(perm);
                }
                default:
                    break;
            }
        }
        return false;
    }
}
