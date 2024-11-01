package ru.overwrite.ublocker.listeners.chat;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
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
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.utils.configuration.data.BookCharsSettings;

public class BookChecker implements Listener {

    private final Main plugin;
    private final BookCharsSettings bookCharsSettings;
    private final Runner runner;

    public BookChecker(Main plugin) {
        this.plugin = plugin;
        this.bookCharsSettings = plugin.getPluginConfig().getBookCharsSettings();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEvent(PlayerEditBookEvent e) {
        if (bookCharsSettings == null) return;

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
        runner.runAsync(() -> {
            p.sendMessage(bookCharsSettings.message());
            if (bookCharsSettings.enableSounds()) {
                Utils.sendSound(bookCharsSettings.sound(), p);
            }
            if (bookCharsSettings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message)};

                String notifyMessage = Utils.replaceEach(bookCharsSettings.notifyMessage(), searchList, replacementList);

                final Component comp = Utils.createHoverMessage(notifyMessage);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(comp);
                        if (bookCharsSettings.notifySoundsEnabled()) {
                            Utils.sendSound(bookCharsSettings.notifySound(), admin);
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
        switch (bookCharsSettings.mode()) {
            case STRING: {
                char[] characters = message.toLowerCase().toCharArray();
                for (char character : characters) {
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
        return switch (bookCharsSettings.mode()) {
            case STRING -> Character.toString(message.codePoints()
                    .filter(codePoint -> bookCharsSettings.string().indexOf(codePoint) == -1).findFirst()
                    .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = bookCharsSettings.pattern().asMatchPredicate();
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
