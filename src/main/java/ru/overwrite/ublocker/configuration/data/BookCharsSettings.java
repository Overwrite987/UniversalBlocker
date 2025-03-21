package ru.overwrite.ublocker.configuration.data;

import it.unimi.dsi.fastutil.chars.CharSet;
import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.regex.Pattern;

public record BookCharsSettings(
        String message,
        boolean enableSounds,
        String[] sound,
        boolean notifyEnabled,
        String notifyMessage,
        boolean notifySoundsEnabled,
        String[] notifySound,
        BlockType mode,
        CharSet charSet,
        Pattern pattern
) {
}
