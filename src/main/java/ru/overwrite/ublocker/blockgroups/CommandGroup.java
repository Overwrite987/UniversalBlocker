package ru.overwrite.ublocker.blockgroups;

import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public record CommandGroup(
        String groupId,
        BlockType blockType,
        boolean blockAliases,
        Set<String> commandsToBlockString,
        Set<Pattern> commandsToBlockPattern,
        List<Condition> conditionsToCheck,
        List<Action> actionsToExecute
) {

    public CommandGroup(String groupId,
                        BlockType blockType,
                        boolean blockAliases,
                        List<String> commandsToBlock,
                        List<Condition> conditionsToCheck,
                        List<Action> actionsToExecute) {
        this(
                groupId,
                blockType,
                blockAliases,
                blockType == BlockType.STRING ? GroupUtils.createStringSet(commandsToBlock) : null,
                blockType == BlockType.PATTERN ? GroupUtils.createPatternSet(commandsToBlock) : null,
                conditionsToCheck,
                actionsToExecute
        );
    }
}
