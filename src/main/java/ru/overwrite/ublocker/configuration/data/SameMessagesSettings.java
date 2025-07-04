package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.actions.Action;

import java.util.List;

public record SameMessagesSettings(
        int samePercents,
        int maxSameMessage,
        int minMessageLength,
        int historySize,
        List<Action> actionsToExecute
) {
}
