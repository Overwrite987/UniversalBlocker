package ru.overwrite.ublocker.listeners.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.BanWordsSettings;
import ru.overwrite.ublocker.configuration.data.CancellationSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (plugin.isAdmin(p, "ublocker.bypass.banwords")) {
            return;
        }
        String message = e.getMessage().toLowerCase();
        switch (banWordsSettings.mode()) {
            case STRING: {
                for (String banword : banWordsSettings.banWordsString()) {
                    if (message.contains(banword)) {
                        blockBanWord(p, banword, message, e, banWordsSettings);
                    }
                }
                break;
            }
            case PATTERN: {
                for (Pattern banword : banWordsSettings.banWordsPattern()) {
                    Matcher matcher = banword.matcher(message);
                    if (matcher.find()) {
                        blockBanWord(p, matcher.group(), message, e, banWordsSettings);
                    }
                }
                break;
            }
        }
    }

    private void blockBanWord(Player p, String banword, String message, AsyncPlayerChatEvent e, BanWordsSettings banWordsSettings) {
        if (banWordsSettings.block()) {
            e.setCancelled(true);
            executeBlockActions(p, banword, message, banWordsSettings.cancellationSettings());
        } else {
            notifyAdmins(p, banword, message, banWordsSettings.cancellationSettings());
            String censored = "*".repeat(banword.length());
            e.setMessage(message.replace(banword, censored));
        }
    }

    private void executeBlockActions(Player p, String banword, String message, CancellationSettings cancellationSettings) {
        p.sendMessage(cancellationSettings.message().replace("%word%", banword));
        Utils.sendSound(cancellationSettings.sound(), p);
        notifyAdmins(p, banword, message, cancellationSettings);
    }

    private final String[] searchList = {"%player%", "%word%", "%msg%"};

    private void notifyAdmins(Player p, String banword, String message, CancellationSettings cancellationSettings) {
        if (cancellationSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), banword, message};

            String formattedMessage = Utils.replaceEach(cancellationSettings.notifyMessage(), searchList, replacementList);

            Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(component);
                    Utils.sendSound(cancellationSettings.notifySound(), admin);
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }
}
