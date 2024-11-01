package ru.overwrite.ublocker.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.Main;

public final class Utils {

    public static String SERIALIZER;

    public static final int SUB_VERSION = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);

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

    public static Component createHoverMessage(String message) {
        String formattedNotifyMessage = Utils.getHoverTextedMessage(message);
        String hovertext = Utils.getHoverText(message);
        HoverEvent<Component> hover = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hovertext));
        return LegacyComponentSerializer.legacySection()
                .deserialize(formattedNotifyMessage.replace(hovertext, ""))
                .hoverEvent(hover);
    }

    private static boolean isHovertexted(String str) {
        return str.contains("ht=");
    }

    private static String getHoverTextedMessage(String str) {
        return isHovertexted(str) ? str.split("ht=")[0] : str;
    }

    private static String getHoverText(String str) {
        return isHovertexted(str) ? str.split("ht=")[1] : "";
    }

    private static final char COLOR_CHAR = '§';

    public static String colorize(String message) {
        switch (SERIALIZER) {
            case "LEGACY": {
                if (SUB_VERSION >= 16) {
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
                }
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

    private static final CharSet CODES = new CharOpenHashSet(
            new char[]{
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'b', 'c', 'd', 'e', 'f',
                    'A', 'B', 'C', 'D', 'E', 'F',
                    'k', 'l', 'm', 'n', 'o', 'r', 'x',
                    'K', 'L', 'M', 'N', 'O', 'R', 'X'}
    );

    private static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length - 1; ++i) {
            if (b[i] == altColorChar && CODES.contains(b[i + 1])) {
                b[i++] = '§';
                b[i] = Character.toLowerCase(b[i]);
            }
        }

        return new String(b);
    }

    public static String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
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
        plugin.getRunner().runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION")
                            .openStream()))) {
                consumer.accept(reader.readLine().trim());
            } catch (IOException exception) {
                plugin.getLogger().warning("Can't check for updates: " + exception.getMessage());
            }
        });
    }
}
