package ru.overwrite.ublocker.configuration.data;

import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record BanWordsSettings(
        BlockType mode,
        Set<String> banWordsString,
        Set<Pattern> banWordsPattern,
        boolean strict,
        String censorSymbol,
        boolean stripColor,
        List<Action> actionsToExecute
) {
}
