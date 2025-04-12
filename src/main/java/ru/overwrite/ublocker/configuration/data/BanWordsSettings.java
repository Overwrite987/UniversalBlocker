package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.Set;
import java.util.regex.Pattern;

public record BanWordsSettings(
        BlockType mode,
        Set<String> banWordsString,
        Set<Pattern> banWordsPattern,
        boolean block,
        CancellationSettings cancellationSettings
) {
}
