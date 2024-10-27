package ru.overwrite.ublocker.listeners.chat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class NumbersCheck implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    public static boolean enabled = false;

    public NumbersCheck(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    private static final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatNumber(AsyncPlayerChatEvent e) {
        if (!enabled) return;
        String message = e.getMessage();
        Player p = e.getPlayer();
        if (pluginConfig.strict_numbers_check) {
            int count = 0;
            for (int a = 0, b = message.length(); a < b; a++) {
                char c = message.charAt(a);
                if (Character.isDigit(c)) {
                    count++;
                }
            }
            if (count > pluginConfig.maxmsgnumbers && !isAdmin(p)) {
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
            if (digitsCount > pluginConfig.maxmsgnumbers && !isAdmin(p)) {
                cancelChatEvent(p, message, e);
            }
        }
    }

    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    private void cancelChatEvent(Player p, String message, Cancellable e) {
        e.setCancelled(true);
        p.sendMessage(pluginConfig.numbers_check_message.replace("%limit%", Integer.toString(pluginConfig.maxmsgnumbers)));
        if (pluginConfig.numbers_check_enable_sounds) {
            p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.numbers_check_sound_id),
                    pluginConfig.numbers_check_sound_volume, pluginConfig.numbers_check_sound_pitch);
        }
        if (pluginConfig.numbers_check_notify) {

            String[] replacementList = {p.getName(), Integer.toString(pluginConfig.maxmsgnumbers), message};

            String notifyMessage = Utils.replaceEach(pluginConfig.numbers_check_notify_message, searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (pluginConfig.numbers_check_notify_sounds) {
                        admin.playSound(admin.getLocation(), Sound.valueOf(pluginConfig.numbers_check_notify_sound_id),
                                pluginConfig.numbers_check_notify_sound_volume,
                                pluginConfig.numbers_check_notify_sound_pitch);
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
