package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.SignCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class SignFilter extends ChatListener {

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    public SignFilter(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignMessage(SignChangeEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.signchars")) {
            return;
        }
        SignCharsSettings signCharsSettings = pluginConfig.getSignCharsSettings();
        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        String[] messages = {line0, line1, line2, line3};
        for (String message : messages) {
            if (message == null || message.isBlank())
                continue;
            if (containsBlockedChars(message, signCharsSettings)) {
                e.setCancelled(true);
                String[] replacementList = {p.getName(), getFirstBlockedChar(message, signCharsSettings), line0 + line1 + line2 + line3};
                super.executeActions(p, searchList, replacementList, signCharsSettings.actionsToExecute());
            }
            break;
        }
    }

    private boolean containsBlockedChars(String message, SignCharsSettings settings) {
        return switch (settings.mode()) {
            case STRING -> Utils.containsInvalidCharacters(message, settings.charSet());
            case PATTERN -> !settings.pattern().matcher(message).matches();
        };
    }

    private String getFirstBlockedChar(String message, SignCharsSettings settings) {
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