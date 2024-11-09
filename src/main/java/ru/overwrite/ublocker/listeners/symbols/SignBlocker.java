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
import org.bukkit.event.block.SignChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

public class SignBlocker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public SignBlocker(Main plugin) {
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
        for (SymbolGroup group : pluginConfig.symbolBlockGroupSet) {
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("sign")) {
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, line0, line1, line2, line3, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, combined, line0, line1, line2, line3, group);
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
            ObjectList<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (line0.contains(symbol) || line1.contains(symbol) || line2.contains(symbol) || line3.contains(symbol)) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, line0, line1, line2, line3, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(SignChangeEvent e, Player p, String combined, String line0, String line1, String line2, String line3, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            ObjectList<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(combined.replace("\n", ""));
            if (matcher.find()) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, line0, line1, line2, line3, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%line0%", "%line1%", "%line2%", "%line3%"};

    private void executeActions(Cancellable e, Player p, String line0, String line1, String line2, String line3, String symbol, ObjectList<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, symbol, line0, line1, line2, line3};
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
                        String formattedMessage = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

                        String messageToPlayer = Utils.extractMessage(formattedMessage, new String[]{"ht={"});
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
                    String logMessage = Utils.extractMessage(action.context(), new String[]{"file={"});
                    String file = Utils.extractValue(action.context(), "file={", "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

                        String notifyMessage = Utils.extractMessage(formattedMessage, new String[]{"ht={", "perm={"});
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
                        String[] sound = Utils.extractMessage(action.context(), new String[]{"perm={"}).split(";");
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
