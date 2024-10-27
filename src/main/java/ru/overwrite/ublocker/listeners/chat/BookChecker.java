package ru.overwrite.ublocker.listeners.chat;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class BookChecker implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    private final Runner runner;
    public static boolean enabled = false;

    public BookChecker(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEvent(PlayerEditBookEvent e) {
        if (!enabled) return;
        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        List<String> messages = e.getNewBookMeta().getPages();
        for (String message : messages) {
            String serialisedMessage = message.replace("\n", "");
            if (containsBlockedChars(serialisedMessage)) {
                cancelBookEvent(p, serialisedMessage, e);
            }
        }
    }

    private final String[] searchList = {"%player%", "%symbol%"};

    private void cancelBookEvent(Player p, String message, Cancellable e) {
        e.setCancelled(true);
        Runnable run = () -> {
            p.sendMessage(pluginConfig.allowed_book_chars_message);
            if (pluginConfig.allowed_book_chars_enable_sounds) {
                p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.allowed_book_chars_sound_id),
                        pluginConfig.allowed_book_chars_sound_volume, pluginConfig.allowed_book_chars_sound_pitch);
            }
            if (pluginConfig.allowed_book_chars_notify) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message)};

                String notifyMessage = Utils.replaceEach(pluginConfig.allowed_book_chars_notify_message, searchList, replacementList);

                final Component comp = Utils.createHoverMessage(notifyMessage);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(comp);
                        if (pluginConfig.allowed_book_chars_notify_sounds) {
                            admin.playSound(admin.getLocation(),
                                    Sound.valueOf(pluginConfig.allowed_book_chars_notify_sound_id),
                                    pluginConfig.allowed_book_chars_notify_sound_volume,
                                    pluginConfig.allowed_book_chars_notify_sound_pitch);
                        }
                    }
                }
                if (plugin.getPluginMessage() != null) {
                    String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                    plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
                }
            }
        };
        runner.runAsync(run);
    }

    private boolean containsBlockedChars(String message) {
        switch (pluginConfig.allowed_book_chars_mode) {
            case STRING: {
                char[] characters = message.toLowerCase().toCharArray();
                for (char character : characters) {
                    if (pluginConfig.allowed_book_chars_string.indexOf(character) == -1)
                        return true;
                }
                break;

            }
            case PATTERN: {
                return !pluginConfig.allowed_book_chars_pattern.matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        return switch (pluginConfig.allowed_book_chars_mode) {
            case STRING -> Character.toString(message.codePoints()
                    .filter(codePoint -> pluginConfig.allowed_book_chars_string.indexOf(codePoint) == -1).findFirst()
                    .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = pluginConfig.allowed_book_chars_pattern.asMatchPredicate();
                yield Character.toString(
                        message.codePoints().filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst().getAsInt());
            }
        };
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.bookchars") || plugin.isExcluded(player));
    }
}
