package ru.overwrite.ublocker.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.Main;

public final class Utils {

    public static String SERIALIZER;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    public static void sendTitleMessage(@NotNull String[] titleMessages, @NotNull Player p) {
        if (titleMessages.length > 5) {
            Bukkit.getConsoleSender().sendMessage("Unable to send title. " + Arrays.toString(titleMessages));
            return;
        }
        String title = titleMessages[0];
        String subtitle = titleMessages.length >= 2 ? titleMessages[1] : "";
        int fadeIn = titleMessages.length >= 3 ? Integer.parseInt(titleMessages[2]) : 10;
        int stay = titleMessages.length >= 4 ? Integer.parseInt(titleMessages[3]) : 70;
        int fadeOut = titleMessages.length == 5 ? Integer.parseInt(titleMessages[4]) : 20;
        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void sendSound(@NotNull String[] soundArgs, @NotNull Player p) {
        if (soundArgs.length > 3) {
            Bukkit.getConsoleSender().sendMessage("Unable to send sound. " + Arrays.toString(soundArgs));
            return;
        }
        Sound sound = Sound.valueOf(soundArgs[0]);
        float volume = soundArgs.length >= 2 ? Float.parseFloat(soundArgs[1]) : 1.0f;
        float pitch = soundArgs.length == 3 ? Float.parseFloat(soundArgs[2]) : 1.0f;
        p.playSound(p.getLocation(), sound, volume, pitch);
    }

    public static final String[] HOVER_MARKER = new String[]{"ht={"};
    public static final String[] PERM_MARKER = new String[]{"perm={"};
    public static final String[] FILE_MARKER = new String[]{"file={"};
    public static final String[] NOTIFY_MARKERS = new String[]{"ht={", "perm={"};

    public static Component createHoverMessage(String message, String hoverText) {
        HoverEvent<Component> hover = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
        return LegacyComponentSerializer.legacySection()
                .deserialize(message)
                .hoverEvent(hover);
    }

    public static String extractMessage(String message, String[] markers) {
        IntList indices = new IntArrayList();
        for (String marker : markers) {
            int index = message.indexOf(marker);
            if (index != -1) {
                indices.add(index);
            }
        }
        int endIndex = indices.isEmpty() ? message.length() : Collections.min(indices);
        return message.substring(0, endIndex).trim();
    }

    public static String extractValue(String message, String prefix, String suffix) {
        int startIndex = message.indexOf(prefix);
        if (startIndex != -1) {
            startIndex += prefix.length();
            int endIndex = message.indexOf(suffix, startIndex);
            if (endIndex != -1) {
                return message.substring(startIndex, endIndex);
            }
        }
        return "";
    }

    private static final char COLOR_CHAR = '§';

    public static String colorize(String message) {
        switch (SERIALIZER) {
            case "LEGACY": {
                final Matcher matcher = HEX_PATTERN.matcher(message);
                final StringBuilder builder = new StringBuilder(message.length() + 32);
                while (matcher.find()) {
                    final String group = matcher.group(1);
                    matcher.appendReplacement(builder,
                            COLOR_CHAR + "x" +
                                    COLOR_CHAR + group.charAt(0) +
                                    COLOR_CHAR + group.charAt(1) +
                                    COLOR_CHAR + group.charAt(2) +
                                    COLOR_CHAR + group.charAt(3) +
                                    COLOR_CHAR + group.charAt(4) +
                                    COLOR_CHAR + group.charAt(5));
                }
                message = matcher.appendTail(builder).toString();

                return translateAlternateColorCodes('&', message);
            }
            case "MINIMESSAGE": {
                Component component = MiniMessage.miniMessage().deserialize(message);
                return LegacyComponentSerializer.legacySection().serialize(component);
            }
            default: {
                return message;
            }
        }
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length; ++i) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
                b[i++] = '§';
                b[i] |= 0x20;
            }
        }

        return new String(b);
    }

    private static boolean isValidColorCharacter(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                c == 'r' ||
                (c >= 'k' && c <= 'o') ||
                c == 'x' ||
                (c >= 'A' && c <= 'F') ||
                c == 'R' ||
                (c >= 'K' && c <= 'O') ||
                c == 'X';
    }

    public static String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }

    public static String getPermOrDefault(String perm, String defaultPerm) {
        if (perm.isEmpty() || perm.isBlank()) {
            return defaultPerm;
        }
        return perm;
    }

    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text.isEmpty() || searchList.length == 0 || replacementList.length == 0) {
            return text;
        }

        if (searchList.length != replacementList.length) {
            throw new IllegalArgumentException("Search and replacement arrays must have the same length.");
        }

        final StringBuilder result = new StringBuilder(text);

        for (int i = 0; i < searchList.length; i++) {
            final String search = searchList[i];
            final String replacement = replacementList[i];

            int start = 0;

            while ((start = result.indexOf(search, start)) != -1) {
                result.replace(start, start + search.length(), replacement);
                start += replacement.length();
            }
        }

        return result.toString();
    }

    public static void checkUpdates(Main plugin, Consumer<String> consumer) {
        plugin.getRunner().runDelayedAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION")
                                    .openStream()))) {
                consumer.accept(reader.readLine().trim());
            } catch (IOException exception) {
                plugin.getLogger().warning("Can't check for updates: " + exception.getMessage());
            }
        }, 20);
    }
}
