package ru.overwrite.ublocker.listeners.chat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.NumberCheckSettings;

public class NumbersCheck implements Listener {

    private final Main plugin;
    private final Config pluginConfig;

    public NumbersCheck(Main plugin) {
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
            if (count > numberCheckSettings.maxNumbers() && !isAdmin(p)) {
                cancelChatEvent(p, message, e);
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
            if (digitsCount > numberCheckSettings.maxNumbers() && !isAdmin(p)) {
                cancelChatEvent(p, message, e);
            }
        }
    }

    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    private void cancelChatEvent(Player p, String message, Cancellable e) {
        NumberCheckSettings numberCheckSettings = pluginConfig.getNumberCheckSettings();
        e.setCancelled(true);
        p.sendMessage(numberCheckSettings.message().replace("%limit%", Integer.toString(numberCheckSettings.maxNumbers())));
        if (numberCheckSettings.enableSounds()) {
            Utils.sendSound(numberCheckSettings.sound(), p);
        }
        if (numberCheckSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), Integer.toString(numberCheckSettings.maxNumbers()), message};

            String notifyMessage = Utils.replaceEach(numberCheckSettings.notifyMessage(), searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (numberCheckSettings.notifySoundsEnabled()) {
                        Utils.sendSound(numberCheckSettings.notifySound(), p);
                    }
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.numbers") || plugin.isExcluded(player);
    }

}
