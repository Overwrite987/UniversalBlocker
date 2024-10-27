package ru.overwrite.ublocker.listeners.symbols;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class ChatBlocker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public ChatBlocker(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        String command = e.getMessage().toLowerCase();
        for (SymbolGroup group : pluginConfig.symbolBlockGroupSet) {
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("chat")) {
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    for (String symbol : group.getSymbolsToBlock()) {
                        List<Action> actions = group.getActionsToExecute();
                        if (actions.isEmpty()) {
                            continue;
                        }
                        if (command.contains(symbol)) {
                            if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                                continue;
                            }
                            executeActions(e, p, command, symbol, actions, p.getWorld().getName());
                        }
                    }
                    break;
                }
                case PATTERN: {
                    for (Pattern pattern : group.getPatternsToBlock()) {
                        List<Action> actions = group.getActionsToExecute();
                        if (actions.isEmpty()) {
                            continue;
                        }
                        Matcher matcher = pattern.matcher(command);
                        if (matcher.find()) {
                            if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                                continue;
                            }
                            executeActions(e, p, command, matcher.group(), actions, p.getWorld().getName());
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private final String[] searchList = {"%world%", "%symbol%", "%msg%"};
    private final String[] searchListPlus = {"%player%", "%world%", "%msg%", "%symbol%"};

    private void executeActions(Cancellable e, Player p, String command, String symbol, List<Action> actions, String world) {
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    e.setCancelled(true);
                    break;
                }
                case LITE_BLOCK: {
                    String perm = action.context();
                    if (!p.hasPermission(perm)) {
                        e.setCancelled(true);
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        // String hovertext = plugin.getHoverText(coAction);
                        // HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, new
                        // Text(Utils.colorize(hovertext)
                        // .replace("%world%", p.getWorld().getName())
                        // .replace("%symbol%", symbol)
                        // .replace("%cmd%", command)));
                        // BaseComponent[] comp = TextComponent.fromLegacyText(Utils.colorize(message
                        // .replace("%world%", p.getWorld().getName())
                        // .replace("%symbol%", symbol)
                        // .replace("%cmd%", command)
                        // .replace(hovertext, "")));
                        // for (BaseComponent component : comp) {
                        // component.setHoverEvent(hover);
                        // }
                        String[] replacementList = {world, symbol, command};

                        String message = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

                        final Component comp = Utils.createHoverMessage(message);

                        p.sendMessage(comp);
                    };
                    runner.runAsync(run);
                    break;
                }
                case TITLE: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        String coAction = Utils.colorize(action.context());
                        String[] replacementList = {world, symbol, command};
                        String[] titleMessages = Utils.replaceEach(coAction, searchList, replacementList).split(";");
                        Utils.sendTitleMessage(titleMessages, p);
                    };
                    runner.runAsync(run);
                    break;
                }
                case ACTIONBAR: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        String coAction = Utils.colorize(action.context());
                        String[] replacementList = {world, symbol, command};
                        String message = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(message);
                    };
                    runner.runAsync(run);
                }
                case SOUND: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        String[] sound = action.context().split(";");
                        p.playSound(p.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
                    };
                    runner.runAsync(run);
                    break;
                }
                case CONSOLE: {
                    Runnable run = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
                    runner.run(run);
                    break;
                }
                case LOG: {
                    String[] coAction = action.context().split("file=");
                    String[] replacementList = {p.getName(), world, command, symbol};
                    plugin.logAction(Utils.replaceEach(coAction[0], searchListPlus, replacementList), coAction[1]);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        String[] coAction = action.context().split("perm=");
                        String perm = coAction[1];

                        String[] replacementList = {p.getName(), world, command, symbol};

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
                    };
                    runner.runAsync(run);
                    break;
                }
                case NOTIFY_SOUND: {
                    if (!e.isCancelled())
                        break;
                    Runnable run = () -> {
                        String[] coAction = action.context().split("perm=");
                        String[] sound = coAction[0].split(";");
                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(coAction[1])) {
                                ps.playSound(ps.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
                            }
                        }
                    };
                    runner.runAsync(run);
                    break;
                }
                default:
                    break;
            }
        }
    }
}
