package ru.overwrite.ublocker.listeners.chat;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.utils.configuration.data.ChatCharsSettings;

public class ChatFilter implements Listener {

    private final Main plugin;
    private final ChatCharsSettings chatCharsSettings;

    public ChatFilter(Main plugin) {
        this.plugin = plugin;
        this.chatCharsSettings = plugin.getPluginConfig().getChatCharsSettings();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMessage(AsyncPlayerChatEvent e) {
        if (chatCharsSettings == null) return;

        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage();
        if (containsBlockedChars(message)) {
            cancelChatEvent(p, message, e);
        }
    }

    private final String[] searchList = {"%player%", "%symbol%", "%msg%"};

    private void cancelChatEvent(Player p, String message, Cancellable e) {
        e.setCancelled(true);
        p.sendMessage(chatCharsSettings.message());
        if (chatCharsSettings.enableSounds()) {
           Utils.sendSound(chatCharsSettings.sound(), p);
        }
        if (chatCharsSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), getFirstBlockedChar(message), message};

            String notifyMessage = Utils.replaceEach(chatCharsSettings.notifyMessage(), searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (chatCharsSettings.notifySoundsEnabled()) {
                        Utils.sendSound(chatCharsSettings.notifySound(), admin);
                    }
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    private boolean containsBlockedChars(String message) {
        switch (chatCharsSettings.mode()) {
            case STRING: {
                for (char character : message.toCharArray()) {
                    if (chatCharsSettings.string().indexOf(character) == -1) {
                        return true;
                    }
                }
                break;
            }
            case PATTERN: {
                return !chatCharsSettings.pattern().matcher(message).matches();
            }
        }
        return false;
    }

    private String getFirstBlockedChar(String message) {
        return switch (chatCharsSettings.mode()) {
            case STRING -> Character.toString(message.codePoints()
                    .filter(codePoint -> chatCharsSettings.string().indexOf(codePoint) == -1).findFirst()
                    .getAsInt());
            case PATTERN -> {
                Predicate<String> allowedCharsPattern = chatCharsSettings.pattern().asMatchPredicate();
                yield Character.toString(
                        message.codePoints().filter(codePoint -> !allowedCharsPattern.test(Character.toString(codePoint)))
                                .findFirst().getAsInt());
            }
        };
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.chatchars") || plugin.isExcluded(player));
    }
}
