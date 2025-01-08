package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
public final class SymbolGroup {

    private final String groupId;

    private final BlockType blockType;

    private Set<String> symbolsToBlock;

    private Set<Pattern> patternsToBlock;

    private List<String> excludedCommandsString;

    private List<Pattern> excludedCommandsPattern;

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    private final List<String> blockFactor;

    public SymbolGroup(String groupId,
                       BlockType blockType,
                       List<String> blockFactor,
                       List<String> symbolsToBlock,
                       List<String> excludedCommand,
                       List<Condition> conditionsToCheck,
                       List<Action> actionsToExecute) {
        this.groupId = groupId;
        this.blockType = blockType;
        this.blockFactor = blockFactor;
        this.setupBlockingList(symbolsToBlock);
        this.setupExcludedCommands(excludedCommand);
        this.conditionsToCheck = conditionsToCheck;
        this.actionsToExecute = actionsToExecute;
    }

    private void setupBlockingList(List<String> symbolsToBlock) {
        switch (this.blockType) {
            case STRING: {
                Set<String> symbolsToBlocks = new ObjectOpenHashSet<>(symbolsToBlock.size());
                for (String s : symbolsToBlock) {
                    symbolsToBlocks.add(s.toLowerCase());
                }
                this.symbolsToBlock = ImmutableSet.copyOf(symbolsToBlocks);
                break;
            }
            case PATTERN: {
                Set<Pattern> patternsToBlocks = new ObjectOpenHashSet<>(symbolsToBlock.size());
                for (String s : symbolsToBlock) {
                    Pattern pattern = Pattern.compile(s);
                    patternsToBlocks.add(pattern);
                }
                this.patternsToBlock = ImmutableSet.copyOf(patternsToBlocks);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void setupExcludedCommands(List<String> excludedCommand) {
        switch (this.blockType) {
            case STRING: {
                List<String> excludedCommandsString = new ObjectArrayList<>(excludedCommand.size());
                for (String s : excludedCommand) {
                    excludedCommandsString.add(s.toLowerCase());
                }
                this.excludedCommandsString = ImmutableList.copyOf(excludedCommandsString);
                break;
            }
            case PATTERN: {
                List<Pattern> excludedCommandsPattern = new ObjectArrayList<>(excludedCommand.size());
                for (String s : excludedCommand) {
                    Pattern pattern = Pattern.compile(s);
                    excludedCommandsPattern.add(pattern);
                }
                this.excludedCommandsPattern = ImmutableList.copyOf(excludedCommandsPattern);
                break;
            }
            default: {
                break;
            }
        }
    }
}
