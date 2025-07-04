package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.ChatCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class ChatFilter extends ChatListener {

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    public ChatFilter(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMessage(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.chatchars")) {
            return;
        }
        ChatCharsSettings chatCharsSettings = pluginConfig.getChatCharsSettings();
        String message = e.getMessage();
        if (containsBlockedChars(message, chatCharsSettings)) {
            String[] replacementList = {p.getName(), getFirstBlockedChar(message, chatCharsSettings), message};
            super.executeActions(e, p, searchList, replacementList, chatCharsSettings.actionsToExecute());
        }
    }

    private boolean containsBlockedChars(String message, ChatCharsSettings chatCharsSettings) {
        return switch (chatCharsSettings.mode()) {
            case STRING -> Utils.containsInvalidCharacters(message, chatCharsSettings.charSet());
            case PATTERN -> !chatCharsSettings.pattern().matcher(message).matches();
        };
    }

    private String getFirstBlockedChar(String message, ChatCharsSettings chatCharsSettings) {
        return switch (chatCharsSettings.mode()) {
            case STRING -> Character.toString(Utils.getFirstBlockedChar(message, chatCharsSettings.charSet()));
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = chatCharsSettings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints()
                                .filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst()
                                .orElseThrow()
                );
            }
        };
    }
}
