package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class CommandFilter implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;
    public static boolean enabled = false;

    public CommandFilter(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void oncommandMessage(PlayerCommandPreprocessEvent e) {
        if (!enabled) return;
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
            p.sendMessage(pluginConfig.allowed_command_chars_message);
            if (pluginConfig.allowed_command_chars_enable_sounds) {
                p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.allowed_command_chars_sound_id),
                        pluginConfig.allowed_command_chars_sound_volume,
                        pluginConfig.allowed_command_chars_sound_pitch);
            }
            if (pluginConfig.allowed_command_chars_notify) {

                String[] replacementList = {p.getName(), getFirstBlockedChar(message), message};

                String notifyMessage = Utils.replaceEach(pluginConfig.allowed_command_chars_notify_message, searchList, replacementList);

                final Component comp = Utils.createHoverMessage(notifyMessage);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(comp);
                        if (pluginConfig.allowed_command_chars_notify_sounds) {
                            admin.playSound(admin.getLocation(),
                                    Sound.valueOf(pluginConfig.allowed_command_chars_notify_sound_id),
                                    pluginConfig.allowed_command_chars_notify_sound_volume,
                                    pluginConfig.allowed_command_chars_notify_sound_pitch);
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
        switch (pluginConfig.allowed_command_chars_mode) {
            case STRING: {
                char[] characters = message.toCharArray();
                for (char character : characters) {
                    if (pluginConfig.allowed_command_chars_string.indexOf(character) == -1)
                        return true;
                }
                break;

            }
            case PATTERN: {
                return !pluginConfig.allowed_command_chars_pattern.matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        switch (pluginConfig.allowed_command_chars_mode) {
            case STRING: {
                char[] characters = message.toLowerCase().toCharArray();
                for (char character : characters) {
                    if (pluginConfig.allowed_command_chars_string.indexOf(character) == -1) {
                        return Character.toString(character);
                    }
                }
                break;
            }
            case PATTERN: {
                String allowedCharsPattern = pluginConfig.allowed_command_chars_pattern.pattern();
                char[] characters = message.toLowerCase().toCharArray();
                for (char character : characters) {
                    if (!Character.toString(character).matches(allowedCharsPattern)) {
                        return Character.toString(character);
                    }
                }
                break;
            }
        }
        return "";
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.commandchars") || plugin.isExcluded(player));
    }
}
