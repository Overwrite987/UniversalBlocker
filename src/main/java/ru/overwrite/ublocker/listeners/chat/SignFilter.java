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

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.utils.configuration.data.SignCharsSettings;

public class SignFilter implements Listener {

    private final Main plugin;
    private final SignCharsSettings signCharsSettings;
    private final Runner runner;

    public SignFilter(Main plugin) {
        this.plugin = plugin;
        this.signCharsSettings = plugin.getPluginConfig().getSignCharsSettings();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignMessage(SignChangeEvent e) {
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
            p.sendMessage(signCharsSettings.message());
            if (signCharsSettings.enableSounds()) {
                Utils.sendSound(signCharsSettings.sound(), p);
            }
            if (signCharsSettings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message), message};

                String notifyMessage = Utils.replaceEach(signCharsSettings.notifyMessage(), searchList, replacementList);

                final Component comp = Utils.createHoverMessage(notifyMessage);

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
        switch (signCharsSettings.mode()) {
            case STRING: {
                char[] characters = message.toCharArray();
                for (char character : characters) {
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