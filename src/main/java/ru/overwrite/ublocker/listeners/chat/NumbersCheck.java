package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.NumberCheckSettings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumbersCheck implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    public NumbersCheck(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    private static final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatNumber(AsyncPlayerChatEvent e) {
        NumberCheckSettings numberCheckSettings = pluginConfig.getNumberCheckSettings();
        if (numberCheckSettings == null) return;

        String message = e.getMessage();
        Player p = e.getPlayer();
        if (numberCheckSettings.strictCheck()) {
            int count = 0;
            for (int a = 0, b = message.length(); a < b; a++) {
                char c = message.charAt(a);
                if (Character.isDigit(c)) {
                    count++;
                }
            }
            if (count > numberCheckSettings.maxNumbers() && !plugin.isAdmin(p, "ublocker.bypass.numbers")) {
                String[] replacementList = {p.getName(), Integer.toString(numberCheckSettings.maxNumbers()), message};
                BlockingUtils.cancelEvent(p, searchList, replacementList, e, numberCheckSettings.cancellationSettings(), plugin.getPluginMessage());
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
            if (digitsCount > numberCheckSettings.maxNumbers() && !plugin.isAdmin(p, "ublocker.bypass.numbers")) {
                String[] replacementList = {p.getName(), Integer.toString(numberCheckSettings.maxNumbers()), message};
                BlockingUtils.cancelEvent(p, searchList, replacementList, e, numberCheckSettings.cancellationSettings(), plugin.getPluginMessage());
            }
        }
    }
}
