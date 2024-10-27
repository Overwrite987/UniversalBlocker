package ru.overwrite.ublocker.listeners.commands;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class CommandBlocker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public CommandBlocker(Main plugin) {
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
        for (CommandGroup group : pluginConfig.commandBlockGroupSet) {
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
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                if (executeActions(e, p, com, command, actions, aliases, p.getWorld().getName())) {
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
                List<String> aliases = comInMap == null ? Collections.emptyList() : new ArrayList<>(comInMap.getAliases());
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                if (aliases.contains(matcher.group())) {
                    if (executeActions(e, p, matcher.group(), command, actions, aliases, p.getWorld().getName())) {
                        break;
                    }
                }
                if (executeActions(e, p, matcher.group(), command, actions, aliases, p.getWorld().getName())) {
                    break;
                }
            }
        }
    }

    private final String[] searchList = {"%world%", "%cmd%", "%fullcmd%"};
    private final String[] searchListPlus = {"%player%", "%world%", "%cmd%", "%fullcmd%"};

    public boolean executeActions(Cancellable e, Player p, String com, String command, List<Action> actions, List<String> aliases, String world) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    List<String> contextList = action.context().contains(",")
                            ? List.of(action.context().split(","))
                            : List.of(action.context());
                    if (contextList.get(0).isBlank()) {
                        e.setCancelled(true);
                        break;
                    }
                    String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (executedCommandBase.replace("/", "").equalsIgnoreCase(s)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    break;
                }
                case LITE_BLOCK: {
                    plugin.getLogger().info("LITE_BLOCK");
                    String[] coAction = action.context().split("perm=");
                    List<String> contextList = coAction[0].contains(",") ? List.of(coAction[0].trim().split(","))
                            : List.of(coAction[0].trim());
                    if (contextList.get(0).isBlank()) {
                        e.setCancelled(true);
                        break;
                    }
                    String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        if (!p.hasPermission(coAction[1])) {
                            e.setCancelled(true);
                            break;
                        }
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (executedCommandBase.replace("/", "").equalsIgnoreCase(s) && !p.hasPermission(coAction[1])) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    break;
                }
                case BLOCK_ARGUMENTS: {
                    if (command.split(" ").length <= 1) {
                        break;
                    }
                    List<String> contextList = action.context().contains(",")
                            ? List.of(action.context().split(","))
                            : List.of(action.context());
                    if (contextList.get(0).isBlank()) {
                        e.setCancelled(true);
                        break;
                    }
                    String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        e.setCancelled(true);
                        break;
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (executedCommandBase.replace("/", "").equalsIgnoreCase(s)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    break;
                }
                case LITE_BLOCK_ARGUMENTS: {
                    if (command.split(" ").length <= 1) {
                        break;
                    }
                    String[] coAction = action.context().split("perm=");
                    List<String> contextList = coAction[0].contains(",") ? List.of(coAction[0].trim().split(","))
                            : List.of(coAction[0].trim());
                    if (contextList.get(0).isBlank()) {
                        e.setCancelled(true);
                        break;
                    }
                    String executedCommandBase = command.contains(" ") ? Utils.cutCommand(command) : command;
                    if (contextList.contains("single") && com.equals(executedCommandBase)) {
                        if (!p.hasPermission(coAction[1])) {
                            e.setCancelled(true);
                            break;
                        }
                    }
                    if (contextList.contains("aliases")) {
                        for (String s : aliases) {
                            if (executedCommandBase.replace("/", "").equalsIgnoreCase(s) && !p.hasPermission(coAction[1])) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] replacementList = {world, com, command};

                        String message = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

                        final Component comp = Utils.createHoverMessage(message);

                        p.sendMessage(comp);
                    });
                    break;
                }
                case TITLE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String coAction = Utils.colorize(action.context());
                        String[] replacementList = {world, com, command};
                        String[] titleMessages = Utils.replaceEach(coAction, searchList, replacementList).split(";");
                        Utils.sendTitleMessage(titleMessages, p);
                    });
                    break;
                }
                case ACTIONBAR: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String coAction = Utils.colorize(action.context());
                        String[] replacementList = {world, com, command};
                        String message = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(message);
                    });
                }
                case SOUND: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] sound = action.context().split(";");
                        p.playSound(p.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
                    });
                    break;
                }
                case CONSOLE: {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
                    break;
                }
                case LOG: {
                    String[] coAction = action.context().split("file=");
                    String[] replacementList = {p.getName(), world, com, command};
                    plugin.logAction(Utils.replaceEach(coAction[0], searchListPlus, replacementList), coAction[1]);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] coAction = action.context().split("perm=");
                        String perm = coAction[1];

                        String[] replacementList = {p.getName(), world, com, command};

                        String notifyMessage = Utils.replaceEach(Utils.colorize(coAction[0]), searchListPlus, replacementList);

                        final Component comp = Utils.createHoverMessage(notifyMessage);

                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(perm)) {
                                ps.sendMessage(comp);
                            }
                        }
                        if (plugin.getPluginMessage() != null) {
                            String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                            plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
                        }
                    });
                    break;
                }
                case NOTIFY_SOUND: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] coAction = action.context().split("perm=");
                        String[] sound = coAction[0].split(";");
                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(coAction[1])) {
                                ps.playSound(ps.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
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