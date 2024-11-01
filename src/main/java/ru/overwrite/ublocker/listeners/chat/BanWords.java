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
import ru.overwrite.ublocker.utils.configuration.data.BanWordsSettings;

public class BanWords implements Listener {

    private final Main plugin;
    private final BanWordsSettings banWordsSettings;

    public BanWords(Main plugin) {
        this.plugin = plugin;
        this.banWordsSettings = plugin.getPluginConfig().getBanWordsSettings();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (banWordsSettings == null) return;

        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage().toLowerCase();
        switch (banWordsSettings.mode()) {
            case STRING: {
                for (String banword : banWordsSettings.banWordsString()) {
                    if (message.contains(banword) && !isAdmin(p)) {
                        if (banWordsSettings.block()) {
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
                for (Pattern banword : banWordsSettings.banWordsPattern()) {
                    Matcher matrcher = banword.matcher(message);
                    if (matrcher.find() && !isAdmin(p)) {
                        if (banWordsSettings.block()) {
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
        p.sendMessage(banWordsSettings.message().replace("%word%", banword));
        if (banWordsSettings.enableSounds()) {
            Utils.sendSound(banWordsSettings.sound(), p);
        }
        notifyAdmins(p, banword, message);
    }

    private final String[] searchList = {"%player%", "%word%", "%msg%"};

    private void notifyAdmins(Player p, String banword, String message) {
        if (banWordsSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), banword, message};

            String notifyMessage = Utils.replaceEach(banWordsSettings.notifyMessage(), searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (banWordsSettings.notifySoundsEnabled()) {
                        Utils.sendSound(banWordsSettings.notifySound(), admin);
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
