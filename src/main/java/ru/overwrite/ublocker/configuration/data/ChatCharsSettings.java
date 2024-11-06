package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.regex.Pattern;

public record ChatCharsSettings(
        String message,
        boolean enableSounds,
        String[] sound,
        boolean notifyEnabled,  // Изменено с `notify`
        String notifyMessage,
        boolean notifySoundsEnabled, // Изменено с `notifySounds`
        String[] notifySound,
        BlockType mode,
        String string,
        Pattern pattern
) { }