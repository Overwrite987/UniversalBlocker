package ru.overwrite.ublocker.blockgroups;

import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record SymbolGroup(
        String groupId,
        BlockType blockType,
        List<String> blockFactor,
        Set<String> symbolsToBlock,
        Set<Pattern> patternsToBlock,
        List<String> excludedCommandsString,
        List<Pattern> excludedCommandsPattern,
        List<Condition> conditionsToCheck,
        List<Action> actionsToExecute
) {

    public SymbolGroup(
            String groupId,
            BlockType blockType,
            List<String> blockFactor,
            List<String> symbolsToBlock,
            List<String> excludedCommand,
            List<Condition> conditionsToCheck,
            List<Action> actionsToExecute
    ) {
        this(
                groupId,
                blockType,
                blockFactor,
                blockType == BlockType.STRING ? GroupUtils.createStringSet(symbolsToBlock) : null,
                blockType == BlockType.PATTERN ? GroupUtils.createPatternSet(symbolsToBlock) : null,
                blockType == BlockType.STRING ? GroupUtils.createStringList(excludedCommand) : null,
                blockType == BlockType.PATTERN ? GroupUtils.createPatternList(excludedCommand) : null,
                conditionsToCheck,
                actionsToExecute
        );
    }
}
