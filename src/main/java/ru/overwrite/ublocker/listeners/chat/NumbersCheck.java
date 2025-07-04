package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.NumberCheckSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumbersCheck extends ChatListener {

    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    public NumbersCheck(UniversalBlocker plugin) {
        super(plugin);
    }

    private static final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatNumber(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.numbers")) {
            return;
        }
        NumberCheckSettings numberCheckSettings = pluginConfig.getNumberCheckSettings();
        String message = e.getMessage();
        if (numberCheckSettings.stripColor()) {
            message = Utils.stripColorCodes(message);
        }
        if (numberCheckSettings.strictCheck()) {
            int count = 0;
            for (int a = 0, b = message.length(); a < b; a++) {
                char c = message.charAt(a);
                if (Character.isDigit(c)) {
                    count++;
                }
            }
            if (count > numberCheckSettings.maxNumbers()) {
                String[] replacementList = {p.getName(), Integer.toString(numberCheckSettings.maxNumbers()), message};
                super.executeActions(e, p, searchList, replacementList, numberCheckSettings.actionsToExecute());
            }
        } else {
            Matcher matcher = IP_PATTERN.matcher(message);
            int digitsCount = 0;

            while (matcher.find()) {
                String[] parts = matcher.group().split("\\.");
                for (String part : parts) {
                    digitsCount += part.length();
                }
            }
            if (digitsCount > numberCheckSettings.maxNumbers()) {
                String[] replacementList = {p.getName(), Integer.toString(numberCheckSettings.maxNumbers()), message};
                super.executeActions(e, p, searchList, replacementList, numberCheckSettings.actionsToExecute());
            }
        }
    }
}
