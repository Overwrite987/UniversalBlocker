package ru.overwrite.ublocker.listeners.symbols;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
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
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_SYMBOLS);
            if (group.blockFactor().isEmpty() || !group.blockFactor().contains("sign")) {
                Utils.printDebug("Group " + group.groupId() + " does not have 'sign' block factor. Skipping...", Utils.DEBUG_SYMBOLS);
                continue;
            }
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.conditionsToCheck())) {
                Utils.printDebug("Blocking does not fulfill the requirements. Skipping group...", Utils.DEBUG_SYMBOLS);
                continue;
            }
            switch (group.blockType()) {
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
        for (String symbol : group.symbolsToBlock()) {
            if (line0.contains(symbol) || line1.contains(symbol) || line2.contains(symbol) || line3.contains(symbol)) {
                String combined = line0 + line1 + line2 + line3;
                Utils.printDebug("Sign message '" + combined + "' contains blocked symbol" + symbol + ". (String)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                executeActions(e, p, combined, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(SignChangeEvent e, Player p, String combined, SymbolGroup group) {
        for (Pattern pattern : group.patternsToBlock()) {
            Matcher matcher = pattern.matcher(combined.replace("\n", ""));
            if (matcher.find()) {
                Utils.printDebug("Sign message '" + combined + "' contains blocked symbol" + matcher.group() + ". (Patten)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                executeActions(e, p, combined, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%msg%"};

    public void executeActions(Cancellable e, Player p, String combined, String symbol, List<Action> actions, String world) {
        Utils.printDebug("Starting executing actions for player '" + p.getName() + "' and blocked symbol '" + symbol + "' (SIGN)", Utils.DEBUG_SYMBOLS);
        final String[] replacementList = {p.getName(), world, symbol, combined};

        for (Action action : actions) {
            ActionType type = action.type();

            if (shouldBlockAction(type, p, action)) {
                Utils.printDebug("Sign event blocked for player '" + p.getName() + "'", Utils.DEBUG_SYMBOLS);
                e.setCancelled(true);
                continue;
            }

            if (e.isCancelled()) {
                switch (type) {
                    case MESSAGE -> sendMessageAsync(p, action, replacementList);
                    case TITLE -> sendTitleAsync(p, action, replacementList);
                    case ACTIONBAR -> sendActionBarAsync(p, action, replacementList);
                    case SOUND -> sendSoundAsync(p, action);
                    case CONSOLE -> executeConsoleCommand(p, action);
                    case LOG -> logAction(action, replacementList);
                    case NOTIFY -> sendNotifyAsync(p, action, replacementList);
                    case NOTIFY_CONSOLE -> sendNotifyConsoleAsync(action, replacementList);
                    case NOTIFY_SOUND -> sendNotifySoundAsync(action);
                }
            }
        }
    }

    private boolean shouldBlockAction(ActionType type, Player p, Action action) {
        return switch (type) {
            case BLOCK -> true;
            case LITE_BLOCK -> !hasBypassPermission(p, action);
            default -> false;
        };
    }

    private void sendMessageAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            Component component = Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS);
            p.sendMessage(component);
        });
    }

    private void sendTitleAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            String[] titleMessages = formattedMessage.split(";");
            Utils.sendTitleMessage(titleMessages, p);
        });
    }

    private void sendActionBarAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String message = formatActionMessage(action, replacementList);
            p.sendActionBar(message);
        });
    }

    private void sendSoundAsync(Player p, Action action) {
        runner.runAsync(() -> Utils.sendSound(action.context().split(";"), p));
    }

    private void executeConsoleCommand(Player p, Action action) {
        runner.run(() -> Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                action.context().replace("%player%", p.getName())
        ));
    }

    private void logAction(Action action, String[] replacementList) {
        String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
        String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
        plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
    }

    private void sendNotifyAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String formattedMessage = formatActionMessage(action, replacementList);
            Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(perm))
                    .forEach(player -> player.sendMessage(component));

            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serialize(component);
                plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
            }
        });
    }

    private void sendNotifyConsoleAsync(Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            Bukkit.getConsoleSender().sendMessage(formattedMessage);
        });
    }

    private void sendNotifySoundAsync(Action action) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER).split(";");

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(perm))
                    .forEach(player -> Utils.sendSound(sound, player));
        });
    }

    private String formatActionMessage(Action action, String[] replacementList) {
        return Utils.replaceEach(
                Utils.COLORIZER.colorize(action.context()),
                searchList,
                replacementList
        );
    }

    private boolean hasBypassPermission(Player p, Action action) {
        return p.hasPermission(getActionPermission(action, "ublocker.bypass.symbols"));
    }

    private String getActionPermission(Action action, String defaultPerm) {
        return Utils.getPermOrDefault(
                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                defaultPerm
        );
    }
}
