package ru.overwrite.ublocker.blockgroups;

import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record SymbolGroup(
        String groupId,
        BlockType blockType,
        Set<BlockFactor> blockFactor,
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
            Set<BlockFactor> blockFactor,
            List<String> symbolsToBlock,
            List<String> excludedCommand,
            List<Condition> conditionsToCheck,
            List<Action> actionsToExecute
    ) {
        this(
                groupId,
                blockType,
                blockFactor,
                blockType.isString() ? GroupUtils.createStringSet(symbolsToBlock) : null,
                blockType.isPattern() ? GroupUtils.createPatternSet(symbolsToBlock) : null,
                blockType.isString() ? GroupUtils.createStringList(excludedCommand) : null,
                blockType.isPattern() ? GroupUtils.createPatternList(excludedCommand) : null,
                conditionsToCheck,
                actionsToExecute
        );
    }
}
