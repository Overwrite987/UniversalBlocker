package ru.overwrite.ublocker.listeners.chat;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.SameMessagesSettings;
import ru.overwrite.ublocker.utils.objects.Pair;

import java.util.Map;

public class SameMessageLimiter extends ChatListener {

    private final Map<String, Pair<Buffer, Double>> sent = new Object2ObjectOpenHashMap<>();
    private final String[] searchList = {"%player%", "%msg%"};

    public SameMessageLimiter(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatSameMessage(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.samemessage")) {
            return;
        }
        SameMessagesSettings sameMessagesSettings = pluginConfig.getSameMessagesSettings();
        String message = e.getMessage();
        String playerName = p.getName();
        if (checkMessage(playerName, message, sameMessagesSettings)) {
            String[] replacementList = {playerName, message};
            super.cancelEvent(p, searchList, replacementList, e, sameMessagesSettings.cancellationSettings(), plugin.getPluginMessage());
        }
    }

    public boolean checkMessage(String playerName, String message, SameMessagesSettings sameMessagesSettings) {
        Pair<Buffer, Double> pair = sent.get(playerName);
        if (pair == null) {
            sent.put(playerName, Pair.of(new Buffer(message, sameMessagesSettings.historySize()), 0d));
            return false;
        }

        final Buffer buffer = pair.left();
        int same = 0;
        for (int i = 0; i < buffer.size(); i++) {
            final String oldMessage = buffer.get(i);

            final double similarity = message.equals(oldMessage)
                    ? 100.0
                    : similarityPercentage(message, oldMessage);

            if (similarity >= sameMessagesSettings.samePercents()) {
                if (++same > sameMessagesSettings.maxSameMessage()) {
                    pair.right(pair.right() + 1);
                    return false;
                }
            }
        }

        buffer.add(message);
        pair.right(Math.max(pair.right() - sameMessagesSettings.reduce(), 0d));
        return true;
    }

    public static double similarityPercentage(String s1, String s2) {
        if (s2 == null) {
            return 0d;
        }
        final int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 100.0;
        }
        final int distance = levenshteinDistance(s1, s2);
        return (1.0 - (double) distance / maxLength) * 100;
    }

    private static int levenshteinDistance(String s, String t) {
        final int m = s.length();
        final int n = t.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }

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
        sent.remove(e.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        sent.remove(e.getPlayer().getName());
    }

    private static final class Buffer {

        private final String[] buffer;
        private int start;
        private int size;

        private Buffer(String message, int bufferSize) {
            this.buffer = new String[bufferSize];
            this.buffer[0] = message;
            this.size = 1;
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
