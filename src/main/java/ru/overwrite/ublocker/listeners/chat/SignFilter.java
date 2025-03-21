package ru.overwrite.ublocker.listeners.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.SignCharsSettings;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

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
        if (plugin.isAdmin(p, "ublocker.bypass.signchars")) return;

        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        List<String> messages = Arrays.asList(line0, line1, line2, line3);
        for (String message : messages) {
            if (message == null || message.isBlank())
                continue;
            if (containsBlockedChars(message, signCharsSettings)) {
                cancelSignEvent(p, message, e, signCharsSettings);
            }
            break;
        }
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    private void cancelSignEvent(Player p, String message, Cancellable e, SignCharsSettings settings) {
        e.setCancelled(true);
        runner.runAsync(() -> {
            p.sendMessage(settings.message());

            if (settings.enableSounds()) {
                Utils.sendSound(settings.sound(), p);
            }

            if (settings.notifyEnabled()) {
                String[] replacementList = {p.getName(), getFirstBlockedChar(message, settings), message};

                String formattedMessage = Utils.replaceEach(settings.notifyMessage(), searchList, replacementList);

                Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("ublocker.admin")) {
                        admin.sendMessage(component);
                        if (settings.notifySoundsEnabled()) {
                            Utils.sendSound(settings.notifySound(), admin);
                        }
                    }
                }

                if (plugin.getPluginMessage() != null) {
                    String gsonMessage = GsonComponentSerializer.gson().serialize(component);
                    plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
                }
            }
        });
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