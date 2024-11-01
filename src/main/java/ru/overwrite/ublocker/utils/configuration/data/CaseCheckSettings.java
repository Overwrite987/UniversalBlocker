package ru.overwrite.ublocker.utils.configuration.data;

public record CaseCheckSettings(
        int maxUpperCasePercent,
        boolean strictCheck,
        String message,
        boolean enableSounds,
        String[] sound,
        boolean notifyEnabled,
        String notifyMessage,
        boolean notifySoundsEnabled,
        String[] notifySound
) { }

