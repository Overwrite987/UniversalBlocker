package ru.overwrite.ublocker.configuration.data;

public record CancellationSettings(
        String message,
        String[] sound,
        boolean notifyEnabled,
        String notifyMessage,
        String[] notifySound
) {
}
