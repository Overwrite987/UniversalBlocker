package ru.overwrite.ublocker.listeners.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public CommandBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        String command = e.getMessage().toLowerCase();
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            if (Utils.DEBUG) {
                plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
                plugin.getPluginLogger().info("Block type: " + group.getBlockType());
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, command, group);
                    break;
                }
                case PATTERN: {
                    checkPatternGroup(e, p, command, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(PlayerCommandPreprocessEvent e, Player p, String command, CommandGroup group) {
        for (String com : group.getCommandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = Utils.cutCommand(command);
            if (executedCommandBase.equalsIgnoreCase(com) || aliases.contains(executedCommandBase.substring(1))) {
                List<Action> actions = group.getActionsToExecute();
                if (actions.isEmpty()) {
                    continue;
                }
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                if (executeActions(group, e, p, com, command, actions, aliases, p.getWorld().getName())) {
                    break;
                }
            }
        }
    }

    private void checkPatternGroup(PlayerCommandPreprocessEvent e, Player p, String command, CommandGroup group) {
        for (Pattern pattern : group.getCommandsToBlockPattern()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                continue;
            }
            Matcher matcher = pattern.matcher(Utils.cutCommand(command).replace("/", ""));
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (aliases.contains(matcher.group())) {
                    if (executeActions(group, e, p, matcher.group(), command, actions, aliases, p.getWorld().getName())) {
                        break;
                    }
                }
                if (executeActions(group, e, p, matcher.group(), command, actions, aliases, p.getWorld().getName())) {
                    break;
                }
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%cmd%", "%fullcmd%"};

    public boolean executeActions(CommandGroup group, Cancellable e, Player p, String com, String command, List<Action> actions, List<String> aliases, String world) {
        final String[] replacementList = {p.getName(), world, com, command};
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (executedCommandBase.substring(1).equalsIgnoreCase(alias)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    if (com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case LITE_BLOCK: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                            "ublocker.bypass.commands");
                    if (p.hasPermission(perm)) {
                        break;
                    }
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (executedCommandBase.substring(1).equalsIgnoreCase(alias)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    if (com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case BLOCK_ARGUMENTS: {
                    if (command.split(" ").length <= 1) {
                        break;
                    }
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (executedCommandBase.substring(1).equalsIgnoreCase(alias)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    if (com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case LITE_BLOCK_ARGUMENTS: {
                    if (command.split(" ").length <= 1) {
                        break;
                    }
                    String perm = Utils.getPermOrDefault(Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"), "ublocker.bypass.commands");
                    if (p.hasPermission(perm)) {
                        break;
                    }
                    String executedCommandBase = Utils.cutCommand(command);
                    if (group.isBlockAliases()) {
                        for (String alias : aliases) {
                            if (executedCommandBase.substring(1).equalsIgnoreCase(alias)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    if (com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);

                        String message = Utils.extractMessage(formattedMessage, Utils.HOVER_MARKERS);
                        String hoverText = Utils.extractValue(formattedMessage, Utils.HOVER_TEXT_PREFIX, "}");
                        String clickEvent = Utils.extractValue(formattedMessage, Utils.CLICK_EVENT_PREFIX, "}");

                        Component component = LegacyComponentSerializer.legacySection().deserialize(message);
                        if (hoverText != null) {
                            component = Utils.createHoverEvent(component, hoverText);
                        }
                        if (clickEvent != null) {
                            component = Utils.createClickEvent(component, clickEvent);
                        }

                        p.sendMessage(component);
                    });
                    break;
                }
                case TITLE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String[] titleMessages = Utils.replaceEach(coAction, searchList, replacementList).split(";");
                        Utils.sendTitleMessage(titleMessages, p);
                    });
                    break;
                }
                case ACTIONBAR: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String message = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(message);
                    });
                    break;
                }
                case SOUND: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] sound = action.context().split(";");
                        Utils.sendSound(sound, p);
                    });
                    break;
                }
                case CONSOLE: {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
                    break;
                }
                case LOG: {
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
                    String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");

                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);

                        String notifyMessage = Utils.extractMessage(formattedMessage, Utils.NOTIFY_MARKERS);
                        String hoverText = Utils.extractValue(formattedMessage, Utils.HOVER_TEXT_PREFIX, "}");
                        String clickEvent = Utils.extractValue(formattedMessage, Utils.CLICK_EVENT_PREFIX, "}");

                        Component component = LegacyComponentSerializer.legacySection().deserialize(notifyMessage);
                        if (hoverText != null) {
                            component = Utils.createHoverEvent(component, hoverText);
                        }
                        if (clickEvent != null) {
                            component = Utils.createClickEvent(component, clickEvent);
                        }

                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(perm)) {
                                ps.sendMessage(component);
                            }
                        }
                        if (plugin.getPluginMessage() != null) {
                            String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                            plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
                        }
                    });
                    break;
                }
                case NOTIFY_SOUND: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");
                        String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER).split(";");
                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(perm)) {
                                Utils.sendSound(sound, ps);
                            }
                        }
                    });
                    break;
                }
                default:
                    break;
            }
        }
        return e.isCancelled();
    }
}