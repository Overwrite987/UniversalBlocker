package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.actions.Action;

import java.util.List;

public record SameMessagesSettings(
        int samePercents,
        int maxSameMessage,
        int minMessageLength,
        int historySize,
        boolean stripColor,
        List<Action> actionsToExecute
) {
}
