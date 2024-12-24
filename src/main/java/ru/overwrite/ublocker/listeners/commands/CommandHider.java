package ru.overwrite.ublocker.listeners.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;

public class CommandHider implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public CommandHider(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        e.getCommands().removeIf(command -> {
            for (CommandGroup group : pluginConfig.getCommandHideGroupSet()) {
                if (Utils.DEBUG) {
                    plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
                    plugin.getPluginLogger().info("Block type: " + group.getBlockType());
                }
                if (checkStringBlock(p, command, group)) {
                    return true;
                }
            }
            return false;
        });
    }

    private boolean checkStringBlock(Player p, String command, CommandGroup group) {
        for (String com : group.getCommandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com);
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            if (command.equalsIgnoreCase(com) || aliases.contains(command)) {
                List<Action> actions = group.getActionsToExecute();
                if (actions.isEmpty()) {
                    continue;
                }
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                if (shouldHideCommand(group, p, com, command, aliases, actions)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldHideCommand(CommandGroup group, Player p, String com, String command, List<String> aliases, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case HIDE: {
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (com.equalsIgnoreCase(alias)) {
                                return true;
                            }
                        }
                    }
                    return com.equals(command);
                }
                case LITE_HIDE: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), Utils.PERM_TEXT_PREFIX, "}"),
                            "ublocker.bypass.commands");
                    if (p.hasPermission(perm)) {
                        return false;
                    }
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (com.equalsIgnoreCase(alias)) {
                                return true;
                            }
                        }
                    }
                    return com.equals(command);
                }
                default:
                    break;
            }
        }
        return false;
    }
}
