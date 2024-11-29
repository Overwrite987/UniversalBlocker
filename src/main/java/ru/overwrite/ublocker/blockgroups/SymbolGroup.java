package ru.overwrite.ublocker.blockgroups;

import java.util.List;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

@Getter
public final class SymbolGroup {

    private final String groupId;

    private final BlockType blockType;

    private final ObjectList<String> symbolsToBlock = new ObjectArrayList<>();

    private final ObjectList<Pattern> patternsToBlock = new ObjectArrayList<>();

    private final ObjectList<String> excludedCommandsString = new ObjectArrayList<>();

    private final ObjectList<Pattern> excludedCommandsPattern = new ObjectArrayList<>();

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    private final ObjectList<String> blockFactor;

    public SymbolGroup(String groupId,
                       BlockType blockType,
                       ObjectList<String> blockFactor,
                       ObjectList<String> symbolsToBlock,
                       ObjectList<String> excludedCommand,
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
                for (String s : symbolsToBlock) {
                    this.symbolsToBlock.add(s.toLowerCase());
                }
                break;
            }
            case PATTERN: {
                for (String s : symbolsToBlock) {
                    Pattern pattern = Pattern.compile(s);
                    this.patternsToBlock.add(pattern);
                }
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
                for (String s : excludedCommand) {
                    excludedCommandsString.add(s.toLowerCase());
                }
                break;
            }
            case PATTERN: {
                for (String s : excludedCommand) {
                    Pattern pattern = Pattern.compile(s);
                    excludedCommandsPattern.add(pattern);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
}
