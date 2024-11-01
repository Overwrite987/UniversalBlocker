package ru.overwrite.ublocker.listeners.commands;

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
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.utils.Utils;
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
            Command comInMap = Bukkit.getCommandMap().getCommand(command);
            if (comInMap == null) {
                return false;
            }
            List<String> aliases = new ArrayList<>(comInMap.getAliases());
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            List<Action> actions = pluginConfig.commandHideStringActions.get(command);
            if (actions != null) {
                if (!ConditionChecker.isMeetsRequirements(p, pluginConfig.commandHideStringConditions.get(command))) {
                    return false;
                }
                return shouldHideCommand(p, command, command, aliases, actions);
            } else {
                // Если в конфиге нет действий для этой команды,
                // то проверяем алиасы и присваиваем соответствующие действия
                for (String alias : aliases) {
                    List<Action> actionsForAlias = pluginConfig.commandHideStringActions.get(alias);
                    if (actionsForAlias != null) {
                        if (!ConditionChecker.isMeetsRequirements(p, pluginConfig.commandHideStringConditions.get(command))) {
                            return false;
                        }
                        // Присвоить действия для этой команды из плагин конфига
                        pluginConfig.commandHideStringActions.put(command, actionsForAlias);
                        return shouldHideCommand(p, command, alias, aliases, actionsForAlias);
                    }
                }
            }
            return false;
        });
    }

    private boolean shouldHideCommand(Player p, String com, String command, List<String> aliases, List<Action> actions) {
        for (Action action : actions) {
            switch (action.type()) {
                case HIDE: {
                    List<String> contextList = Utils.getContextList(action.context());
                    if (contextList.get(0).isBlank()) {
                        return true;
                    }
                    if (contextList.contains("single") && com.equals(command)) {
                        return true;
                    }
                    if (contextList.contains("aliases")) {
                        for (String alias : aliases) {
                            if (com.equalsIgnoreCase(alias)) {
                                return true;
                            }
                        }
                    }
                    break;
                }
                case LITE_HIDE: {
                    String[] coAction = action.context().split("perm=");
                    List<String> contextList = Utils.getContextList(coAction[0]);
                    if (contextList.get(0).isBlank()) {
                        return true;
                    }
                    if (contextList.contains("single") && com.equals(command)) {
                        if (!p.hasPermission(coAction[1])) {
                            return true;
                        }
                    }
                    if (contextList.contains("aliases")) {
                        for (String alias : aliases) {
                            if (com.equalsIgnoreCase(alias) && !p.hasPermission(coAction[1])) {
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
