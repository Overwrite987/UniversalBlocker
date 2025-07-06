package ru.overwrite.ublocker.listeners.chat;

import lombok.Getter;
import lombok.Setter;
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

public abstract class ChatListener implements Listener {

    protected final UniversalBlocker plugin;
    protected final Config pluginConfig;
    private final Runner runner;

    @Getter
    @Setter
    protected boolean registered;

    public ChatListener(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    public void executeActions(Cancellable e, Player p, String[] searchList, String[] replacementList, List<Action> actions) {
        Utils.printDebug("Starting executing actions for player '" + p.getName() + "'", Utils.DEBUG_CHAT);

        for (Action action : actions) {
            ActionType type = action.type();
            switch (type) {
                case MESSAGE -> sendMessageAsync(p, action, searchList, replacementList);
                case TITLE -> sendTitleAsync(p, action, searchList, replacementList);
                case ACTIONBAR -> sendActionBarAsync(p, action, searchList, replacementList);
                case SOUND -> sendSoundAsync(p, action);
                case CONSOLE -> executeConsoleCommand(p, action);
                case LOG -> logAction(action, searchList, replacementList);
                case NOTIFY -> sendNotifyAsync(p, action, searchList, replacementList);
                case NOTIFY_CONSOLE -> sendNotifyConsoleAsync(action, searchList, replacementList);
                case NOTIFY_SOUND -> sendNotifySoundAsync(action);
            }
        }
    }

    private void sendMessageAsync(Player p, Action action, String[] searchList, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, searchList, replacementList);
            Component component = Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS);
            p.sendMessage(component);
        });
    }

    private void sendTitleAsync(Player p, Action action, String[] searchList, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, searchList, replacementList);
            String[] titleMessages = formattedMessage.split(";");
            Utils.sendTitleMessage(titleMessages, p);
        });
    }

    private void sendActionBarAsync(Player p, Action action, String[] searchList, String[] replacementList) {
        runner.runAsync(() -> {
            String message = formatActionMessage(action, searchList, replacementList);
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

    private void logAction(Action action, String[] searchList, String[] replacementList) {
        String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
        String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
        plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
    }

    private void sendNotifyAsync(Player p, Action action, String[] searchList, String[] replacementList) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String formattedMessage = formatActionMessage(action, searchList, replacementList);
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

    private void sendNotifyConsoleAsync(Action action, String[] searchList, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, searchList, replacementList);
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

    private String formatActionMessage(Action action, String[] searchList, String[] replacementList) {
        return Utils.replaceEach(
                Utils.COLORIZER.colorize(action.context()),
                searchList,
                replacementList
        );
    }

    private boolean hasBypassPermission(Player p, Action action) {
        return p.hasPermission(getActionPermission(action, "ublocker.bypass.chat"));
    }

    private String getActionPermission(Action action, String defaultPerm) {
        return Utils.getPermOrDefault(
                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                defaultPerm
        );
    }

    protected boolean isAdmin(Player player, String permission) {
        return player.hasPermission(permission) || plugin.isExcluded(player);
    }
}
