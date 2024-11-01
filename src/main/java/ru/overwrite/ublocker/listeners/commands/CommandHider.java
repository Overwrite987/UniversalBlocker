package ru.overwrite.ublocker.listeners.commands;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.utils.configuration.Config;

public class CommandHider implements Listener {

    private final Main plugin;
    private final Config pluginConfig;

    public CommandHider(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        e.getCommands().removeIf(command -> {
            for (CommandGroup group : pluginConfig.commandHideGroupSet) {
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
            List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
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
                    String perm = action.context();
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
