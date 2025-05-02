package ru.overwrite.ublocker.configuration.data;

public record SameMessagesSettings(
        int samePercents,
        int maxSameMessage,
        double reduce,
        int historySize,
        CancellationSettings cancellationSettings
) {
}
