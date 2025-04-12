package ru.overwrite.ublocker.configuration.data;

public record SameMessagesSettings(
        int maxSameMessage,
        boolean strict,
        int samePercents,
        CancellationSettings cancellationSettings
) {
}
