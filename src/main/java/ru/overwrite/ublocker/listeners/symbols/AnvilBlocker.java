package ru.overwrite.ublocker.listeners.symbols;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.utils.Utils;

public class AnvilBlocker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public AnvilBlocker(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        if (e.getSlot() != 2)
            return;
        ItemStack resultItem = e.getCurrentItem();
        if (resultItem == null || !resultItem.hasItemMeta() || !resultItem.getItemMeta().hasDisplayName()) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        if (plugin.isExcluded(p))
            return;
        String name = resultItem.getItemMeta().getDisplayName();
        for (SymbolGroup group : pluginConfig.symbolBlockGroupSet) {
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("anvil")) {
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, name, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, name, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(InventoryClickEvent e, Player p, String name, SymbolGroup group) {
        for (String symbol : group.getSymbolsToBlock()) {
            ObjectList<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (name.contains(symbol)) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, name, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(InventoryClickEvent e, Player p, String name, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            ObjectList<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                    continue;
                }
                executeActions(e, p, name, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%cmd%"};

    private void executeActions(Cancellable e, Player p, String name, String symbol, ObjectList<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, name, symbol};
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
                    String file = Utils.extractValue(action.context(), "file={", "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        break;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.colorize(action.context()), searchList, replacementList);

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