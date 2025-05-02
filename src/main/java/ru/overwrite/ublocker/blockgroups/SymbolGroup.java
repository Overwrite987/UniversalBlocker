package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
                blockType == BlockType.STRING ? createStringSet(symbolsToBlock) : null,
                blockType == BlockType.PATTERN ? createPatternSet(symbolsToBlock) : null,
                blockType == BlockType.STRING ? createStringList(excludedCommand) : null,
                blockType == BlockType.PATTERN ? createPatternList(excludedCommand) : null,
                conditionsToCheck,
                actionsToExecute
        );
    }

    private static ImmutableSet<String> createStringSet(List<String> input) {
        Set<String> set = new ObjectOpenHashSet<>(input.size());
        for (String s : input) {
            set.add(s.toLowerCase());
        }
        return ImmutableSet.copyOf(set);
    }

    private static ImmutableSet<Pattern> createPatternSet(List<String> input) {
        Set<Pattern> set = new ObjectOpenHashSet<>(input.size());
        for (String s : input) {
            set.add(Pattern.compile(s));
        }
        return ImmutableSet.copyOf(set);
    }

    private static ImmutableList<String> createStringList(List<String> input) {
        List<String> list = new ObjectArrayList<>(input.size());
        for (String s : input) {
            list.add(s.toLowerCase());
        }
        return ImmutableList.copyOf(list);
    }

    private static ImmutableList<Pattern> createPatternList(List<String> input) {
        List<Pattern> list = new ObjectArrayList<>(input.size());
        for (String s : input) {
            list.add(Pattern.compile(s));
        }
        return ImmutableList.copyOf(list);
    }
}
