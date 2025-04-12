package ru.overwrite.ublocker.listeners.chat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.SameMessagesSettings;

public class SameMessageLimiter implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Object2ObjectMap<String, String> lastMessages = new Object2ObjectOpenHashMap<>();
    private final Object2IntMap<String> repeatCounter = new Object2IntOpenHashMap<>();
    private final String[] searchList = {"%player%", "%msg%"};

    public SameMessageLimiter(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatSameMessage(AsyncPlayerChatEvent e) {
        SameMessagesSettings sameMessagesSettings = pluginConfig.getSameMessagesSettings();
        if (sameMessagesSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.samemessage")) return;

        String name = p.getName();
        String newMessage = e.getMessage();
        String lastMessage = lastMessages.get(name);

        if (lastMessage != null) {
            boolean isSame;
            if (!sameMessagesSettings.strict()) {
                isSame = lastMessage.equalsIgnoreCase(newMessage);
            } else {
                int similarity = getSimilarityPercent(lastMessage, newMessage);
                isSame = similarity >= sameMessagesSettings.samePercents();
            }

            if (isSame) {
                int count = repeatCounter.getOrDefault(name, 1);
                if (count >= sameMessagesSettings.maxSameMessage()) {
                    String[] replacementList = {p.getName(), newMessage};
                    BlockingUtils.cancelEvent(p, searchList, replacementList, e, sameMessagesSettings.cancellationSettings(), plugin.getPluginMessage());
                    return;
                } else {
                    repeatCounter.put(name, count + 1);
                }
            } else {
                repeatCounter.put(name, 1);
            }
        } else {
            repeatCounter.put(name, 1);
        }

        lastMessages.put(name, newMessage);
    }

    private int getSimilarityPercent(String msg1, String msg2) {
        String longer = msg1.length() > msg2.length() ? msg1 : msg2;
        String common = longestCommonSubstring(msg1, msg2);

        if (common.isEmpty()) return 0;
        double percent = (common.length() * 100.0) / longer.length();
        return (int) percent;
    }

    private String longestCommonSubstring(String s1, String s2) {
        int[][] table = new int[s1.length()][s2.length()];
        int maxLen = 0;
        int endIndex = -1;

        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < s2.length(); j++) {
                if (s1.charAt(i) == s2.charAt(j)) {
                    if (i == 0 || j == 0) {
                        table[i][j] = 1;
                    } else {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }

                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                        endIndex = i;
                    }
                }
            }
        }

        return maxLen == 0 ? "" : s1.substring(endIndex - maxLen + 1, endIndex + 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        String name = e.getPlayer().getName();
        lastMessages.remove(name);
        repeatCounter.removeInt(name);
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        String name = e.getPlayer().getName();
        lastMessages.remove(name);
        repeatCounter.removeInt(name);
    }
}
