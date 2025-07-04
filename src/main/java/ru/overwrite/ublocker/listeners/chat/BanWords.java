package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.BanWordsSettings;
import ru.overwrite.ublocker.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanWords extends ChatListener {

    private final String[] searchList = {"%player%", "%word%", "%msg%"};

    public BanWords(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBanWord(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.banwords")) {
            return;
        }
        BanWordsSettings banWordsSettings = pluginConfig.getBanWordsSettings();
        String message = e.getMessage().toLowerCase();
        if (banWordsSettings.stripColor()) {
            message = Utils.stripColorCodes(message);
        }
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
        if (banWordsSettings.strict()) {
            e.setCancelled(true);
            final String[] replacementList = {p.getName(), banword, message};
            executeActions(e, p, searchList, replacementList, banWordsSettings.actionsToExecute());
            return;
        }
        Utils.printDebug("Censored word " + banword, Utils.DEBUG_CHAT);
        String censored = banWordsSettings.censorSymbol().repeat(banword.length());
        e.setMessage(message.replace(banword, censored));
    }
}
