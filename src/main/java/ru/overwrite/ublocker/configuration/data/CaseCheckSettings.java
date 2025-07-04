package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.actions.Action;

import java.util.List;

public record CaseCheckSettings(
        int maxUpperCasePercent,
        boolean strictCheck,
        List<Action> actionsToExecute
) {
}