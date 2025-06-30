package ru.overwrite.ublocker.configuration.data;

public record CaseCheckSettings(
        int maxUpperCasePercent,
        boolean strictCheck,
        CancellationSettings cancellationSettings
) {
}