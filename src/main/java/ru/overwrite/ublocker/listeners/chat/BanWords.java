package ru.overwrite.ublocker.listeners.chat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.BanWordsSettings;

public class BanWords implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public BanWords(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        BanWordsSettings banWordsSettings = pluginConfig.getBanWordsSettings();
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
                    Matcher matcher = banword.matcher(message);
                    if (matcher.find() && !isAdmin(p)) {
                        if (banWordsSettings.block()) {
                            e.setCancelled(true);
                            executeBlockActions(p, matcher.group(), message, e);
                        } else {
                            notifyAdmins(p, matcher.group(), message);
                            String censored = "*".repeat(matcher.group().length());
                            e.setMessage(message.replace(matcher.group(), censored));
                        }
                    }
                }
                break;
            }
        }
    }

    private void executeBlockActions(Player p, String banword, String message, Cancellable e) {
        BanWordsSettings banWordsSettings = pluginConfig.getBanWordsSettings();
        p.sendMessage(banWordsSettings.message().replace("%word%", banword));
        if (banWordsSettings.enableSounds()) {
            Utils.sendSound(banWordsSettings.sound(), p);
        }
        notifyAdmins(p, banword, message);
    }

    private final String[] searchList = {"%player%", "%word%", "%msg%"};

    private void notifyAdmins(Player p, String banword, String message) {
        BanWordsSettings banWordsSettings = pluginConfig.getBanWordsSettings();
        if (banWordsSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), banword, message};

            String formattedMessage = Utils.replaceEach(banWordsSettings.notifyMessage(), searchList, replacementList);

            String notifyMessage = Utils.extractMessage(formattedMessage, Utils.NOTIFY_MARKERS);
            String hoverText = Utils.extractValue(formattedMessage, Utils.HOVER_TEXT_PREFIX, "}");
            String clickEvent = Utils.extractValue(formattedMessage, Utils.CLICK_EVENT_PREFIX, "}");

            Component component = LegacyComponentSerializer.legacySection().deserialize(notifyMessage);
            if (hoverText != null) {
                component = Utils.createHoverEvent(component, hoverText);
            }
            if (clickEvent != null) {
                component = Utils.createClickEvent(component, clickEvent);
            }

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(component);
                    if (banWordsSettings.notifySoundsEnabled()) {
                        Utils.sendSound(banWordsSettings.notifySound(), admin);
                    }
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.banwords") || plugin.isExcluded(player));
    }
}
