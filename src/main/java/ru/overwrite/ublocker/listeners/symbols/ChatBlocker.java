package ru.overwrite.ublocker.listeners.symbols;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

public class ChatBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public ChatBlocker(UniversalBlocker plugin) {
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
            if (Utils.DEBUG) {
                plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
            }
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("chat")) {
                if (Utils.DEBUG) {
                    plugin.getPluginLogger().info("Group " + group.getGroupId() + " does not have 'chat' block factor. Skipping...");
                }
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
            ObjectList<Action> actions = group.getActionsToExecute();
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
            ObjectList<Action> actions = group.getActionsToExecute();
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

    private void executeActions(Cancellable e, Player p, String message, String symbol, ObjectList<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, message, symbol};
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    e.setCancelled(true);
                    break;
                }
                case LITE_BLOCK: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), "perm={", "}"),
                            "ublocker.bypass.symbols");
                    if (!p.hasPermission(perm)) {
                        e.setCancelled(true);
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);

                        String messageToPlayer = Utils.extractMessage(formattedMessage, Utils.HOVER_MARKER);
                        String hoverText = Utils.extractValue(formattedMessage, "ht={", "}");

                        final Component comp = Utils.createHoverMessage(messageToPlayer, hoverText);

                        p.sendMessage(comp);
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
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
                    String file = Utils.extractValue(action.context(), "file={", "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);

                        String notifyMessage = Utils.extractMessage(formattedMessage, Utils.NOTIFY_MARKERS);
                        String hoverText = Utils.extractValue(formattedMessage, "ht={", "}");
                        String perm = Utils.getPermOrDefault(Utils.extractValue(formattedMessage, "perm={", "}"), "ublocker.admin");

                        final Component comp = Utils.createHoverMessage(notifyMessage, hoverText);

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
                        String perm = Utils.extractValue(action.context(), "perm={", "}");
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
    }
}
