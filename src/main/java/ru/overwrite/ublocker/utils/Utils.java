package ru.overwrite.ublocker.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.utils.color.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class Utils {

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

    public static Colorizer COLORIZER;

    public static void setupColorizer(ConfigurationSection mainSettings) {
        COLORIZER = switch (mainSettings.getString("serializer", "LEGACY").toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public static boolean DEBUG;

    public static void printDebug(String messgae) {
        if (DEBUG) {
            Bukkit.getConsoleSender().sendMessage("[UniversalBlocker-Debug] " + messgae);
        }
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

    public static final String PERM_PREFIX = "perm={";
    public static final String FILE_PREFIX = "file={";
    public static final String HOVER_TEXT_PREFIX = "hoverText={";
    public static final String CLICK_EVENT_PREFIX = "clickEvent={";
    public static final String BUTTON_PREFIX = "button={";
    public static final String[] PERM_MARKER = {PERM_PREFIX};
    public static final String[] FILE_MARKER = {FILE_PREFIX};
    public static final String[] HOVER_MARKERS = {HOVER_TEXT_PREFIX, CLICK_EVENT_PREFIX};
    public static final String[] NOTIFY_MARKERS = {HOVER_TEXT_PREFIX, CLICK_EVENT_PREFIX, PERM_PREFIX};

    public static Component parseMessage(String formattedMessage, String[] markers) {
        List<Component> components = new ArrayList<>();
        int currentIndex = 0;

        String globalHoverText = null;
        String globalClickEvent = null;

        while (currentIndex < formattedMessage.length()) {
            int buttonStart = formattedMessage.indexOf(BUTTON_PREFIX, currentIndex);

            if (buttonStart == -1) {
                String remainingText = formattedMessage.substring(currentIndex);

                if (!remainingText.isEmpty()) {
                    globalHoverText = extractValue(remainingText, HOVER_TEXT_PREFIX, "}");
                    globalClickEvent = extractValue(remainingText, CLICK_EVENT_PREFIX, "}");

                    remainingText = extractMessage(remainingText, markers, true);
                    if (!remainingText.isEmpty()) {
                        components.add(LegacyComponentSerializer.legacySection().deserialize(remainingText));
                    }
                }
                break;
            }

            if (buttonStart > currentIndex) {
                String beforeButton = formattedMessage.substring(currentIndex, buttonStart);

                globalHoverText = extractValue(beforeButton, HOVER_TEXT_PREFIX, "}");
                globalClickEvent = extractValue(beforeButton, CLICK_EVENT_PREFIX, "}");

                beforeButton = extractMessage(beforeButton, markers, true);
                if (!beforeButton.isEmpty()) {
                    components.add(LegacyComponentSerializer.legacySection().deserialize(beforeButton));
                }
            }

            int buttonEnd = findClosingBracket(formattedMessage, buttonStart + BUTTON_PREFIX.length());
            if (buttonEnd == -1) {
                throw new IllegalArgumentException("Некорректный формат кнопки: отсутствует закрывающая }");
            }

            String buttonContent = formattedMessage.substring(buttonStart + BUTTON_PREFIX.length(), buttonEnd);
            Component buttonComponent = parseButtonContent(buttonContent);

            boolean hasLeadingSpace = buttonStart > 0 && formattedMessage.charAt(buttonStart - 1) == ' ';
            boolean hasTrailingSpace = buttonEnd + 1 < formattedMessage.length() && formattedMessage.charAt(buttonEnd + 1) == ' ';

            if (hasLeadingSpace) {
                components.add(Component.text(" "));
            }

            components.add(buttonComponent);

            if (hasTrailingSpace) {
                components.add(Component.text(" "));
            }

            currentIndex = buttonEnd + 1;
        }

        Component finalComponent = Component.text().append(components).build();

        if (globalHoverText != null) {
            finalComponent = createHoverEvent(finalComponent, globalHoverText);
        }
        if (globalClickEvent != null) {
            finalComponent = createClickEvent(finalComponent, globalClickEvent);
        }

        return finalComponent;
    }

    private static int findClosingBracket(String message, int startIndex) {
        int depth = 0;
        for (int i = startIndex; i < message.length(); i++) {
            char currentChar = message.charAt(i);
            if (currentChar == '{') {
                depth++;
            } else if (currentChar == '}') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }

        return -1;
    }

    private static Component parseButtonContent(String buttonContent) {
        String buttonText = null;
        String hoverText = null;
        String clickEvent = null;

        String[] parts = buttonContent.split(";");
        for (String part : parts) {
            if (part.startsWith(HOVER_TEXT_PREFIX)) {
                hoverText = extractValue(part, HOVER_TEXT_PREFIX, "}");
            } else if (part.startsWith(CLICK_EVENT_PREFIX)) {
                clickEvent = extractValue(part, CLICK_EVENT_PREFIX, "}");
            } else {
                buttonText = part;
            }
        }

        if (buttonText == null || buttonText.isEmpty()) {
            throw new IllegalArgumentException("Кнопка должна содержать текст.");
        }

        Component buttonComponent = LegacyComponentSerializer.legacySection().deserialize(buttonText);

        if (hoverText != null) {
            buttonComponent = createHoverEvent(buttonComponent, hoverText);
        }

        if (clickEvent != null) {
            buttonComponent = createClickEvent(buttonComponent, clickEvent);
        }

        return buttonComponent;
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
        return null;
    }

    public static String extractMessage(String message, String[] markers, boolean removeMarkers) {
        IntList indices = new IntArrayList();
        for (String marker : markers) {
            int index = message.indexOf(marker);
            if (index != -1) {
                indices.add(index);
            }
        }
        int endIndex = indices.isEmpty() ? message.length() : Collections.min(indices);

        String baseMessage = message.substring(0, endIndex).trim();

        if (removeMarkers) {
            for (String marker : markers) {
                int startIndex = message.indexOf(marker);
                if (startIndex != -1) {
                    int endIndexMarker = findClosingBracket(message, startIndex + marker.length() - 1);
                    if (endIndexMarker != -1) {
                        message = message.substring(0, startIndex).trim() + " " + message.substring(endIndexMarker + 1).trim();
                    }
                }
            }
        }

        return baseMessage.trim();
    }

    public static Component createHoverEvent(Component message, String hoverText) {
        HoverEvent<Component> hover = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
        return message.hoverEvent(hover);
    }

    public static Component createClickEvent(Component message, String clickEvent) {
        String[] clickEventArgs = clickEvent.split(";", 2);
        ClickEvent.Action action = ClickEvent.Action.valueOf(clickEventArgs[0].toUpperCase());
        String context = clickEventArgs[1];
        ClickEvent click = ClickEvent.clickEvent(action, context);
        return message.clickEvent(click);
    }

    public static final char COLOR_CHAR = '§';

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        final char[] b = textToTranslate.toCharArray();

        for (int i = 0, length = b.length - 1; i < length; ++i) {
            if (b[i] == altColorChar && isValidColorCharacter(b[i + 1])) {
                b[i++] = COLOR_CHAR;
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
        if (perm == null || perm.isBlank()) {
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

    public static void checkUpdates(UniversalBlocker plugin, Consumer<String> consumer) {
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
