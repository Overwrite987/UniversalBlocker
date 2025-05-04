package ru.overwrite.ublocker.configuration.data;

public record SameMessagesSettings(
        int samePercents,
        int maxSameMessage,
        int historySize,
        CancellationSettings cancellationSettings
) {
}
