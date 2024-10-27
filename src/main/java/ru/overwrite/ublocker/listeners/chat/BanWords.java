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

public class BanWords implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    public static boolean enabled = false; // Это нужно чем-то потенциально заменить

    public BanWords(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!enabled) return;
        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage().toLowerCase();
        switch (pluginConfig.banwordmode) {
            case STRING: {
                for (String banword : pluginConfig.ban_words_string) {
                    if (message.contains(banword) && !isAdmin(p)) {
                        if (pluginConfig.ban_words_block) {
                            e.setCancelled(true);
                            executeBlockActions(p, banword, message, e);
                        } else {
                            notifyAdmins(p, banword, message);
                            String censored = "*".repeat(banword.length());
                            e.setMessage(message.replace(banword, censored));
                        }
                    }
                }
                break;
            }
            case PATTERN: {
                for (Pattern banword : pluginConfig.ban_words_pattern) {
                    Matcher matrcher = banword.matcher(message);
                    if (matrcher.find() && !isAdmin(p)) {
                        if (pluginConfig.ban_words_block) {
                            e.setCancelled(true);
                            executeBlockActions(p, matrcher.group(), message, e);
                        } else {
                            notifyAdmins(p, matrcher.group(), message);
                            String censored = "*".repeat(matrcher.group().length());
                            e.setMessage(message.replace(matrcher.group(), censored));
                        }
                    }
                }
                break;
            }
        }
    }

    private void executeBlockActions(Player p, String banword, String message, Cancellable e) {
        p.sendMessage(pluginConfig.ban_words_message.replace("%word%", banword));
        if (pluginConfig.ban_words_enable_sounds) {
            p.playSound(p.getLocation(), Sound.valueOf(pluginConfig.ban_words_sound_id),
                    pluginConfig.ban_words_sound_volume, pluginConfig.ban_words_sound_pitch);
        }
        notifyAdmins(p, banword, message);
    }

    private final String[] searchList = {"%player%", "%word%", "%msg%"};

    private void notifyAdmins(Player p, String banword, String message) {
        if (pluginConfig.ban_words_notify) {
            String[] replacementList = {p.getName(), banword, message};

            String notifyMessage = Utils.replaceEach(pluginConfig.ban_words_notify_message, searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (pluginConfig.ban_words_notify_sounds) {
                        admin.playSound(admin.getLocation(), Sound.valueOf(pluginConfig.ban_words_notify_sound_id),
                                pluginConfig.ban_words_notify_sound_volume, pluginConfig.ban_words_notify_sound_pitch);
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
        return (player.hasPermission("ublocker.bypass.banwords") || plugin.isExcluded(player));
    }
}
