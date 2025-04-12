package ru.overwrite.ublocker.configuration.data;

public record NumberCheckSettings(
        int maxNumbers,
        boolean strictCheck,
        CancellationSettings cancellationSettings
) {
}
