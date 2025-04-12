package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.ChatCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class ChatFilter implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    public ChatFilter(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMessage(AsyncPlayerChatEvent e) {
        ChatCharsSettings chatCharsSettings = pluginConfig.getChatCharsSettings();
        if (chatCharsSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.chatchars")) {
            return;
        }
        String message = e.getMessage();
        if (containsBlockedChars(message, chatCharsSettings)) {
            String[] replacementList = {p.getName(), getFirstBlockedChar(message, chatCharsSettings)};
            BlockingUtils.cancelEvent(p, searchList, replacementList, e, chatCharsSettings.cancellationSettings(), plugin.getPluginMessage());
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
