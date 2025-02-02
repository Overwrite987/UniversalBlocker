package ru.overwrite.ublocker.listeners.symbols;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            if (Utils.DEBUG) {
                plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
            }
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("chat")) {
                if (Utils.DEBUG) {
                    plugin.getPluginLogger().info("Group " + group.getGroupId() + " does not have 'chat' block factor. Skipping...");
                }
                continue;
            }
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                if (Utils.DEBUG) {
                    plugin.getPluginLogger().info("Blocking does not fulfill the requirements. Skipping group...");
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
            List<Action> actions = group.getActionsToExecute();
            if (message.contains(symbol)) {
                executeActions(e, p, message, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(AsyncPlayerChatEvent e, Player p, String message, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                List<Action> actions = group.getActionsToExecute();
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
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                            "ublocker.bypass.symbols");
                    if (!p.hasPermission(perm)) {
                        e.setCancelled(true);
                    }
                    break;
                }
                case MESSAGE: {
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Component component = Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS);
                        p.sendMessage(component);
                    });
                    break;
                }
                case TITLE: {
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String[] titleMessages = Utils.replaceEach(coAction, searchList, replacementList).split(";");
                        Utils.sendTitleMessage(titleMessages, p);
                    });
                    break;
                }
                case ACTIONBAR: {
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String messageToPlayer = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(messageToPlayer);
                    });
                    break;
                }
                case SOUND: {
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
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER, true);
                    String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");

                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

                        Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission(perm))
                                .forEach(player -> player.sendMessage(component));

                        if (plugin.getPluginMessage() != null) {
                            String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                            plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
                        }
                    });
                    break;
                }
                case NOTIFY_CONSOLE: {
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Bukkit.getConsoleSender().sendMessage(formattedMessage);
                    });
                    break;
                }
                case NOTIFY_SOUND: {
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");
                        String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER, true).split(";");
                        Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission(perm))
                                .forEach(player -> Utils.sendSound(sound, player));
                    });
                    break;
                }
                default:
                    break;
            }
        }
    }
}
