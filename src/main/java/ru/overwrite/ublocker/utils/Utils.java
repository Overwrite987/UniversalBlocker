package ru.overwrite.ublocker.utils;

import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
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
import java.util.Arrays;
import java.util.function.Consumer;

@UtilityClass
public class Utils {

    public final boolean FOLIA;

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

    private Boolean hasWorldGuard;

    public boolean hasWorldGuard() {
        if (hasWorldGuard == null) {
            try {
                Class.forName("com.sk89q.worldguard.protection.flags.registry.FlagConflictException");
                hasWorldGuard = true;
            } catch (ClassNotFoundException ex) {
                hasWorldGuard = false;
            }
        }
        return hasWorldGuard;
    }

    public boolean USE_PAPI;

    public String parsePlaceholders(String message, Player player) {
        if (PlaceholderAPI.containsPlaceholders(message)) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    public Colorizer COLORIZER;

    public void setupColorizer(ConfigurationSection mainSettings) {
        COLORIZER = switch (mainSettings.getString("serializer", "LEGACY").toUpperCase()) {
            case "MINIMESSAGE" -> new MiniMessageColorizer();
            case "LEGACY" -> new LegacyColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            default -> new VanillaColorizer();
        };
    }

    public boolean DEBUG_CHAT;
    public boolean DEBUG_COMMANDS;
    public boolean DEBUG_SYMBOLS;

    public void printDebug(String messgae, boolean shouldPrint) {
        if (shouldPrint) {
            Bukkit.getConsoleSender().sendMessage("[UniversalBlocker-Debug] " + messgae);
        }
    }

    public void sendTitleMessage(@NotNull String[] titleMessages, @NotNull Player p) {
        if (titleMessages[0].isEmpty()) {
            return;
        }
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

    public void sendSound(@NotNull String[] soundArgs, @NotNull Player p) {
        if (soundArgs[0].isEmpty()) {
            return;
        }
        if (soundArgs.length > 3) {
            Bukkit.getConsoleSender().sendMessage("Unable to send sound. " + Arrays.toString(soundArgs));
            return;
        }
        Sound sound = Sound.valueOf(soundArgs[0]);
        float volume = soundArgs.length >= 2 ? Float.parseFloat(soundArgs[1]) : 1.0F;
        float pitch = soundArgs.length == 3 ? Float.parseFloat(soundArgs[2]) : 1.0F;
        p.playSound(p.getLocation(), sound, volume, pitch);
    }

    public final String PERM_PREFIX = "perm={";
    public final String FILE_PREFIX = "file={";
    public final String HOVER_TEXT_PREFIX = "hoverText={";
    public final String CLICK_EVENT_PREFIX = "clickEvent={";
    public final String BUTTON_PREFIX = "button={";
    public final String[] PERM_MARKER = {PERM_PREFIX};
    public final String[] FILE_MARKER = {FILE_PREFIX};
    public final String[] HOVER_MARKERS = {HOVER_TEXT_PREFIX, CLICK_EVENT_PREFIX};
    public final String[] NOTIFY_MARKERS = {HOVER_TEXT_PREFIX, CLICK_EVENT_PREFIX, PERM_PREFIX};

    public Component parseMessage(String formattedMessage, String[] markers) {
        ObjectList<Component> components = new ObjectArrayList<>();
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

                    remainingText = extractMessage(remainingText, markers);
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

                beforeButton = extractMessage(beforeButton, markers);
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
                components.add(Component.space());
            }

            components.add(buttonComponent);

            if (hasTrailingSpace) {
                components.add(Component.space());
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

    private int findClosingBracket(String message, int startIndex) {
        int depth = 0;
        char[] chars = message.toCharArray();
        for (int i = startIndex; i < chars.length; i++) {
            char currentChar = chars[i];
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

    private Component parseButtonContent(String buttonContent) {
        String buttonText = null;
        String hoverText = null;
        String clickEvent = null;

        ObjectList<String> parts = getParts(buttonContent);

        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            if (part.startsWith(HOVER_TEXT_PREFIX)) {
                hoverText = extractValue(part, HOVER_TEXT_PREFIX, "}");
            } else if (part.startsWith(CLICK_EVENT_PREFIX)) {
                clickEvent = extractValue(part, CLICK_EVENT_PREFIX, "}");
            } else {
                if (buttonText == null) {
                    buttonText = part;
                } else {
                    throw new IllegalArgumentException("Некорректный формат кнопки: несколько текстовых частей.");
                }
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

    private ObjectList<String> getParts(String buttonContent) {
        ObjectList<String> parts = new ObjectArrayList<>();
        int start = 0;
        int depth = 0;
        char[] chars = buttonContent.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            } else if (c == ';' && depth == 0) {
                parts.add(buttonContent.substring(start, i).trim());
                start = i + 1;
            }
        }
        parts.add(buttonContent.substring(start).trim());
        return parts;
    }

    public String extractValue(String message, String prefix, String suffix) {
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

    public String extractMessage(String message, String[] markers) {
        String baseMessage = getBaseMessage(message, markers);

        for (String marker : markers) {
            int startIndex = message.indexOf(marker);
            if (startIndex != -1) {
                int endIndexMarker = findClosingBracket(message, startIndex + marker.length() - 1);
                if (endIndexMarker != -1) {
                    message = message.substring(0, startIndex).trim() + " " + message.substring(endIndexMarker + 1).trim();
                }
            }
        }

        return baseMessage.trim();
    }

    private String getBaseMessage(String message, String[] markers) {
        int endIndex = message.length();

        for (String marker : markers) {
            int idx = message.indexOf(marker);
            if (idx != -1 && idx < endIndex) {
                endIndex = idx;
                if (endIndex == 0) break;
            }
        }

        return endIndex == 0
                ? ""
                : message.substring(0, endIndex).trim();
    }

    public Component createHoverEvent(Component message, String hoverText) {
        HoverEvent<Component> hover = HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
        return message.hoverEvent(hover);
    }

    private Component createClickEvent(Component message, String clickEvent) {
        int separatorIndex = clickEvent.indexOf(';');
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("Некорректный формат clickEvent: отсутствует разделитель ';'");
        }

        String actionStr = clickEvent.substring(0, separatorIndex).trim();
        String context = clickEvent.substring(separatorIndex + 1).trim();

        ClickEvent.Action action = ClickEvent.Action.valueOf(actionStr.toUpperCase());
        ClickEvent click = ClickEvent.clickEvent(action, context);

        return message.clickEvent(click);
    }

    public String stripColorCodes(String text) {
        if (text.length() < 3) {
            return text;
        }

        char[] chars = text.toCharArray();
        int writeIndex = 0;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length && Utils.isValidColorCharacter(chars[i + 1])) {
                i++;
                continue;
            }
            chars[writeIndex++] = chars[i];
        }

        return writeIndex == chars.length ? text : new String(chars, 0, writeIndex);
    }

    public final char COLOR_CHAR = '§';

    public String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        final char[] chars = textToTranslate.toCharArray();

        for (int i = 0, length = chars.length - 1; i < length; i++) {
            if (chars[i] == altColorChar && isValidColorCharacter(chars[i + 1])) {
                chars[i++] = COLOR_CHAR;
                chars[i] |= 0x20;
            }
        }

        return new String(chars);
    }

    private boolean isValidColorCharacter(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D',
                 'E', 'F', 'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }

    public boolean IGNORE_UNKNOWN_COMMANDS;

    public boolean isUnknownCommand(String command) {
        return IGNORE_UNKNOWN_COMMANDS && !Bukkit.getCommandMap().getKnownCommands().containsKey(cutCommand(command));
    }

    public String cutCommand(String str) {
        int index = str.indexOf(' ');
        return index == -1 ? str : str.substring(0, index);
    }

    public boolean containsInvalidCharacters(String str, CharSet charSet) {
        return getFirstBlockedChar(str, charSet) != null;
    }

    public Character getFirstBlockedChar(String str, CharSet charSet) {
        char[] chars = str.toCharArray();
        for (char c : chars) {
            if (!charSet.contains(c)) {
                return c;
            }
        }
        return null;
    }

    public String getPermOrDefault(String perm, String defaultPerm) {
        if (perm == null || perm.isBlank()) {
            return defaultPerm;
        }
        return perm;
    }

    public String replaceEach(String text, String[] searchList, String[] replacementList) {
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

    public void checkUpdates(UniversalBlocker plugin, Consumer<String> consumer) {
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
