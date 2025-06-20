package ru.overwrite.ublocker.configuration.data;

public record SameMessagesSettings(
        int samePercents,
        int maxSameMessage,
        int minMessageLength,
        int historySize,
        CancellationSettings cancellationSettings
) {
}
