package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEditBookEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.BookCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class BookFilter extends ChatListener {

    private final String[] searchList = {"%player%", "%world%", "%symbol%"};

    public BookFilter(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBookEvent(PlayerEditBookEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.bookchars")) {
            return;
        }
        BookCharsSettings bookCharsSettings = pluginConfig.getBookCharsSettings();
        for (String page : e.getNewBookMeta().getPages()) {
            String serialisedMessage = page.replace("\n", "");
            if (containsBlockedChars(serialisedMessage, bookCharsSettings)) {
                e.setCancelled(true);
                String[] replacementList = {p.getName(), getFirstBlockedChar(serialisedMessage, bookCharsSettings)};
                super.executeActions(p, searchList, replacementList, bookCharsSettings.actionsToExecute());
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
