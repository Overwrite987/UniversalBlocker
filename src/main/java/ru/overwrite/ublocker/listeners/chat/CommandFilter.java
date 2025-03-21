package ru.overwrite.ublocker.listeners.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CommandCharsSettings;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.function.Predicate;

public class CommandFilter implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public CommandFilter(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandMessage(PlayerCommandPreprocessEvent e) {
        CommandCharsSettings commandCharsSettings = pluginConfig.getCommandCharsSettings();
        if (commandCharsSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.commandchars")) return;

        String message = e.getMessage();
        if (containsBlockedChars(message, commandCharsSettings)) {
            cancelCommandEvent(p, message, e, commandCharsSettings);
        }
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    private void cancelCommandEvent(Player p, String message, Cancellable e, CommandCharsSettings settings) {
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
