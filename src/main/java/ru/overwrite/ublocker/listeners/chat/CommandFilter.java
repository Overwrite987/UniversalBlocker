package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.CommandCharsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class CommandFilter extends ChatListener {

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    public CommandFilter(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandMessage(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.commandchars")) {
            return;
        }
        CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
        String message = e.getMessage();
        if (containsBlockedChars(message, commandCharsSettings)) {
            String[] replacementList = {p.getName(), getFirstBlockedChar(message, commandCharsSettings)};
            super.executeActions(e, p, searchList, replacementList, commandCharsSettings.actionsToExecute());
        }
    }

    private boolean containsBlockedChars(String message, CommandCharsSettings settings) {
        return switch (settings.mode()) {
            case STRING -> Utils.containsInvalidCharacters(message, settings.charSet());
            case PATTERN -> !settings.pattern().matcher(message).matches();
        };
    }

    private String getFirstBlockedChar(String message, CommandCharsSettings settings) {
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
