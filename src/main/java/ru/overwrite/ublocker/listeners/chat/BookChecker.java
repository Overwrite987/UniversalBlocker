package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.BookCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class BookChecker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    private final String[] searchList = {"%player%", "%symbol%"};

    public BookChecker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEvent(PlayerEditBookEvent e) {
        BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
        if (bookCharsSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.bookchars")) return;

        for (String page : e.getNewBookMeta().getPages()) {
            String serialisedMessage = page.replace("\n", "");
            if (containsBlockedChars(serialisedMessage, bookCharsSettings)) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(serialisedMessage, bookCharsSettings)};
                BlockingUtils.cancelEvent(p, searchList, replacementList, e, bookCharsSettings.cancellationSettings(), plugin.getPluginMessage());
                break;
            }
        }
    }

    private boolean containsBlockedChars(String message, BookCharsSettings settings) {
        return switch (settings.mode()) {
            case STRING -> Utils.containsInvalidCharacters(message, settings.charSet());
            case PATTERN -> !settings.pattern().matcher(message).matches();
        };
    }

    private String getFirstBlockedChar(String message, BookCharsSettings settings) {
        return switch (settings.mode()) {
            case STRING -> Character.toString(Utils.getFirstBlockedChar(message, settings.charSet()));
            case PATTERN -> {
                Predicate<String> predicate = settings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints()
                                .filter(codePoint -> !predicate.test(Character.toString(codePoint)))
                                .findFirst()
                                .orElseThrow()
                );
            }
        };
    }
}
