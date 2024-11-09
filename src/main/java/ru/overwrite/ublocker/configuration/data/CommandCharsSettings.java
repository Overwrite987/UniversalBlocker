package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.regex.Pattern;

public record CommandCharsSettings(
        String message,
        boolean enableSounds,
        String[] sound,
        boolean notifyEnabled,
        String notifyMessage,
        boolean notifySoundsEnabled,
        String[] notifySound,
        BlockType mode,
        String string,
        Pattern pattern
) { }