package ru.overwrite.ublocker.utils.configuration.data;

public record NumberCheckSettings(
        int maxNumbers,
        boolean strictCheck,
        String message,
        boolean enableSounds,
        String[] sound,
        boolean notifyEnabled,
        String notifyMessage,
        boolean notifySoundsEnabled,
        String[] notifySound
) { }

