package ru.overwrite.ublocker.listeners.chat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.SignCharsSettings;

public class SignFilter implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public SignFilter(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignMessage(SignChangeEvent e) {
        SignCharsSettings signCharsSettings = pluginConfig.getSignCharsSettings();
        if (signCharsSettings == null) return;

        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        List<String> messages = Arrays.asList(line0, line1, line2, line3);
        for (String message : messages) {
            if (message == null || message.isBlank())
                continue;
            if (containsBlockedChars(message)) {
                cancelSignEvent(p, message, e);
            }
            break;
        }
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    private void cancelSignEvent(Player p, String message, Cancellable e) {
        e.setCancelled(true);
        runner.runAsync(() -> {
            SignCharsSettings signCharsSettings = pluginConfig.getSignCharsSettings();
            p.sendMessage(signCharsSettings.message());
            if (signCharsSettings.enableSounds()) {
                Utils.sendSound(signCharsSettings.sound(), p);
            }
            if (signCharsSettings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message), message};

                String formattedMessage = Utils.replaceEach(signCharsSettings.notifyMessage(), searchList, replacementList);

                String notifyMessage = Utils.extractMessage(formattedMessage, Utils.HOVER_MARKER);
                String hoverText = Utils.extractValue(formattedMessage, "ht={", "}");

                final Component comp = Utils.createHoverMessage(notifyMessage, hoverText);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(comp);
                        if (signCharsSettings.notifySoundsEnabled()) {
                            Utils.sendSound(signCharsSettings.notifySound(), admin);
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
        SignCharsSettings signCharsSettings = pluginConfig.getSignCharsSettings();
        switch (signCharsSettings.mode()) {
            case STRING: {
                for (char character : message.toCharArray()) {
                    if (signCharsSettings.string().indexOf(character) == -1) {
                        return true;
                    }
                }
                break;
            }
            case PATTERN: {
                return !signCharsSettings.pattern().matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        SignCharsSettings signCharsSettings = pluginConfig.getSignCharsSettings();
        return switch (signCharsSettings.mode()) {
            case STRING -> Character.toString(message.codePoints()
                    .filter(codePoint -> signCharsSettings.string().indexOf(codePoint) == -1).findFirst()
                    .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = signCharsSettings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints().filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst().getAsInt());
            }
        };
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.signchars") || plugin.isExcluded(player));
    }
}