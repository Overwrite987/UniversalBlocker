package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
                blockType == BlockType.STRING ? setupStringSet(commandsToBlock) : null,
                blockType == BlockType.PATTERN ? setupPatternSet(commandsToBlock) : null,
                conditionsToCheck,
                actionsToExecute
        );
    }

    private static ImmutableSet<String> setupStringSet(List<String> commands) {
        Set<String> set = new ObjectOpenHashSet<>(commands.size());
        for (String s : commands) {
            set.add(s.toLowerCase());
        }
        return ImmutableSet.copyOf(set);
    }

    private static ImmutableSet<Pattern> setupPatternSet(List<String> commands) {
        Set<Pattern> set = new ObjectOpenHashSet<>(commands.size());
        for (String s : commands) {
            set.add(Pattern.compile(s));
        }
        return ImmutableSet.copyOf(set);
    }
}
