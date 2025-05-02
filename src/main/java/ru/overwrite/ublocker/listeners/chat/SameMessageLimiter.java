package ru.overwrite.ublocker.listeners.chat;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.SameMessagesSettings;

import java.util.Map;
import java.util.UUID;

public class SameMessageLimiter implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    private final Map<UUID, Pair<Buffer, Double>> sent;
    private final String[] searchList;

    public boolean isRegistered = false;

    public SameMessageLimiter(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.sent = new Object2ObjectOpenHashMap<>();
        this.searchList = new String[]{"%player%", "%msg%"};
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatSameMessage(AsyncPlayerChatEvent e) {
        SameMessagesSettings sameMessagesSettings = pluginConfig.getSameMessagesSettings();
        if (sameMessagesSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.samemessage")) return;

        String message = e.getMessage();

        if (test(p, message)) {
            String[] replacementList = {p.getName(), message};
            BlockingUtils.cancelEvent(p, searchList, replacementList, e, sameMessagesSettings.cancellationSettings(), plugin.getPluginMessage());
        }
    }

    public boolean test(Player p, String message) {
        if (message == null) return false;
        SameMessagesSettings sameMessagesSettings = pluginConfig.getSameMessagesSettings();
        Pair<Buffer, Double> pair = sent.get(p.getUniqueId());
        if (pair == null) {
            sent.put(p.getUniqueId(), Pair.of(new Buffer(message, sameMessagesSettings.historySize()), 0d));
            return false;
        }

        final Buffer buffer = pair.left();
        for (int i = 0; i < buffer.size(); i++) {
            final String oldMessage = buffer.get(i);
            double currentScore = pair.right();

            final double similarity = message.equals(oldMessage)
                    ? 100.0
                    : similarityPercentage(message, oldMessage);

            if (similarity >= sameMessagesSettings.samePercents()) {
                if (++currentScore > sameMessagesSettings.maxSameMessage()) {
                    buffer.add(message);
                    return false;
                }
            }
        }

        buffer.add(message);
        pair.right(Math.max(pair.right() - sameMessagesSettings.reduce(), 0));

        return true;
    }

    public static double similarityPercentage(String s1, @Nullable String s2) {
        if (s2 == null) return 0d;
        final int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 100.0;
        final int distance = levenshteinDistance(s1, s2);
        return (1.0 - (double) distance / maxLength) * 100;
    }

    private static int levenshteinDistance(String s, String t) {
        final int m = s.length();
        final int n = t.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = (s.charAt(i - 1) == t.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[n];
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        sent.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        sent.remove(e.getPlayer().getUniqueId());
    }

    private static final class Buffer {

        private final String[] buffer;
        private int start = 0;
        private int size = 0;

        private Buffer(String message, int bufferSize) {
            this.buffer = new String[bufferSize];
            this.buffer[0] = message;
        }

        public void add(String element) {
            this.buffer[(this.start + this.size) % this.buffer.length] = element;
            if (this.size < this.buffer.length) {
                this.size++;
            } else {
                this.start = (this.start + 1) % this.buffer.length;
            }
        }

        public String get(int index) {
            if (index >= size) {
                throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
            }
            return this.buffer[(this.start + index) % this.buffer.length];
        }

        public int size() {
            return this.size;
        }
    }
}
