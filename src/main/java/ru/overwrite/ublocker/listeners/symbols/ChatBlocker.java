package ru.overwrite.ublocker.listeners.symbols;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
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
import ru.overwrite.ublocker.utils.configuration.Config;
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
        String message = e.getMessage().toLowerCase();
        for (SymbolGroup group : pluginConfig.symbolBlockGroupSet) {
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("chat")) {
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, message, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, message, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(AsyncPlayerChatEvent e, Player p, String message, SymbolGroup group) {
        for (String symbol : group.getSymbolsToBlock()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (message.contains(symbol)) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, message, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(AsyncPlayerChatEvent e, Player p, String message, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, message, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%msg%"};

    private void executeActions(Cancellable e, Player p, String message, String symbol, List<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, message, symbol};
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    e.setCancelled(true);
                    break;
                }
                case LITE_BLOCK: {
                    String perm = Utils.getPermOrDefault(action.context(), "ublocker.bypass.symbols");
                    if (!p.hasPermission(perm)) {
                        e.setCancelled(true);
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
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
                        String messageToPlayer = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

                        final Component comp = Utils.createHoverMessage(messageToPlayer);

                        p.sendMessage(comp);
                    });
                    break;
                }
                case TITLE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String coAction = Utils.colorize(action.context());
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
                        String messageToPlayer = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(messageToPlayer);
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
                    runner.run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName())));
                    break;
                }
                case LOG: {
                    String[] coAction = action.context().split("file=");
                    plugin.logAction(Utils.replaceEach(coAction[0], searchList, replacementList), coAction[1]);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String[] coAction = action.context().split("perm=");
                        String perm = coAction[1];

                        String notifyMessage = Utils.replaceEach(Utils.colorize(coAction[0]), searchList, replacementList);

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
                        String[] sound = coAction[0].trim().split(";");
                        for (Player ps : Bukkit.getOnlinePlayers()) {
                            if (ps.hasPermission(coAction[1])) {
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
    }
}
