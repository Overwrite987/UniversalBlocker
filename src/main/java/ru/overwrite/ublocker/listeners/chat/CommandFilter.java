package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CommandCharsSettings;

import java.util.function.Predicate;

public class CommandFilter implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public CommandFilter(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void oncommandMessage(PlayerCommandPreprocessEvent e) {
        CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
        if (commandCharsSettings == null) return;

        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage();
        if (containsBlockedChars(message)) {
            cancelCommandEvent(p, message, e);
        }
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    private void cancelCommandEvent(Player p, String message, Cancellable e) {
        e.setCancelled(true);
        runner.runAsync(() -> {
            CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
            p.sendMessage(commandCharsSettings.message());
            if (commandCharsSettings.enableSounds()) {
                Utils.sendSound(commandCharsSettings.sound(), p);
            }
            if (commandCharsSettings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message), message};

                String notifyMessage = Utils.replaceEach(commandCharsSettings.notifyMessage(), searchList, replacementList);

                final Component comp = Utils.createHoverMessage(notifyMessage);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(comp);
                        if (commandCharsSettings.notifySoundsEnabled()) {
                            Utils.sendSound(commandCharsSettings.notifySound(), admin);
                        }
                    }
                }
                if (plugin.getPluginMessage() != null) {
                    String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                    plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
                }
            }
        });
    }

    private boolean containsBlockedChars(String message) {
        CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
        switch (commandCharsSettings.mode()) {
            case STRING: {
                for (char character : message.toCharArray()) {
                    if (commandCharsSettings.string().indexOf(character) == -1)
                        return true;
                }
                break;

            }
            case PATTERN: {
                return !commandCharsSettings.pattern().matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
        return switch (commandCharsSettings.mode()) {
            case STRING -> Character.toString(message.codePoints()
                    .filter(codePoint -> commandCharsSettings.string().indexOf(codePoint) == -1).findFirst()
                    .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = commandCharsSettings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints().filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst().getAsInt());
            }
        };
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.commandchars") || plugin.isExcluded(player));
    }
}
