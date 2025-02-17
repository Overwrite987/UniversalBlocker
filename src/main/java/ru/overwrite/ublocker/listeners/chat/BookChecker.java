package ru.overwrite.ublocker.listeners.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.BookCharsSettings;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.function.Predicate;

public class BookChecker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public BookChecker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEvent(PlayerEditBookEvent e) {
        BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
        if (bookCharsSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.bookchars")) {
            return;
        }
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
        runner.runAsync(() -> {
            BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
            p.sendMessage(bookCharsSettings.message());
            if (bookCharsSettings.enableSounds()) {
                Utils.sendSound(bookCharsSettings.sound(), p);
            }
            if (bookCharsSettings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message)};

                String formattedMessage = Utils.replaceEach(bookCharsSettings.notifyMessage(), searchList, replacementList);

                Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(component);
                        if (bookCharsSettings.notifySoundsEnabled()) {
                            Utils.sendSound(bookCharsSettings.notifySound(), admin);
                        }
                    }
                }
                if (plugin.getPluginMessage() != null) {
                    String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                    plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
                }
            }
        });
    }

    private boolean containsBlockedChars(String message) {
        BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
        switch (bookCharsSettings.mode()) {
            case STRING: {
                for (char character : message.toCharArray()) {
                    if (bookCharsSettings.string().indexOf(character) == -1)
                        return true;
                }
                break;

            }
            case PATTERN: {
                return bookCharsSettings.pattern().matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
        return switch (bookCharsSettings.mode()) {
            case STRING -> Character.toString(
                    message.codePoints()
                            .filter(codePoint -> bookCharsSettings.string().indexOf(codePoint) == -1).findFirst()
                            .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = bookCharsSettings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints()
                                .filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst().getAsInt());
            }
        };
    }
}
