package ru.overwrite.ublocker.listeners.symbols;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
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

public class SignBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public SignBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSign(SignChangeEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        String line0 = e.getLine(0).toLowerCase();
        String line1 = e.getLine(1).toLowerCase();
        String line2 = e.getLine(2).toLowerCase();
        String line3 = e.getLine(3).toLowerCase();
        String combined = line0 + line1 + line2 + line3;
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            if (Utils.DEBUG) {
                plugin.getPluginLogger().info("Group checking now: " + group.getGroupId());
            }
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("sign")) {
                if (Utils.DEBUG) {
                    plugin.getPluginLogger().info("Group " + group.getGroupId() + " does not have 'sign' block factor. Skipping...");
                }
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, line0, line1, line2, line3, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, combined, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(SignChangeEvent e, Player p, String line0, String line1, String line2, String line3, SymbolGroup group) {
        for (String symbol : group.getSymbolsToBlock()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (line0.contains(symbol) || line1.contains(symbol) || line2.contains(symbol) || line3.contains(symbol)) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                String combined = line0+line1+line2+line3;
                executeActions(e, p, combined, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(SignChangeEvent e, Player p, String combined, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(combined.replace("\n", ""));
            if (matcher.find()) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, combined, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%msg%"};

    private void executeActions(Cancellable e, Player p, String combined, String symbol, List<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, symbol, combined};
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
                        return;
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
                    String file = Utils.extractValue(action.context(), "file={", "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), "perm={", "}"),
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
                                Utils.extractValue(action.context(), "perm={", "}"),
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
    }
}
