package ru.overwrite.ublocker.listeners.symbols;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;

public abstract class SymbolBlocker implements Listener {

    protected final UniversalBlocker plugin;
    protected final Config pluginConfig;
    private final Runner runner;

    private final String[] searchList = {"%player%", "%world%", "%msg%", "%symbol%",};

    protected SymbolBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    protected void executeActions(Cancellable e, Player p, String fullString, String symbol, List<Action> actions) {
        Utils.printDebug("Starting executing actions for player '" + p.getName() + "' and blocked symbol '" + symbol + "'", Utils.DEBUG_SYMBOLS);
        final String[] replacementList = {p.getName(), p.getWorld().getName(), fullString, symbol};

        for (Action action : actions) {
            ActionType type = action.type();

            if (shouldBlockAction(type, p, action)) {
                Utils.printDebug("Event blocked for player '" + p.getName() + "'", Utils.DEBUG_SYMBOLS);
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
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
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
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
        return Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
    }

    private boolean hasBypassPermission(Player p, Action action) {
        return p.hasPermission(getActionPermission(action, "ublocker.bypass.symbols"));
    }

    private String getActionPermission(Action action, String defaultPerm) {
        return Utils.getPermOrDefault(Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"), defaultPerm);
    }
}
