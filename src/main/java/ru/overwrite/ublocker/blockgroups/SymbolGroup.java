package ru.overwrite.ublocker.blockgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

@Getter
public class SymbolGroup {

    private final String groupId;

    private final BlockType blockType;

    private final List<String> symbolsToBlock = new ArrayList<>();

    private final List<Pattern> patternsToBlock = new ArrayList<>();

    private final List<String> excludedCommandsString = new ArrayList<>();

    private final List<Pattern> excludedCommandsPattern = new ArrayList<>();

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    private final List<String> blockFactor;

    public SymbolGroup(String groupId, BlockType blockType, List<String> blockFactor, List<String> symbolsToBlock, List<String> excludedCommand, List<Condition> conditionsToCheck, List<Action> actionsToExecute) {
        this.groupId = groupId;
        this.blockType = blockType;
        this.blockFactor = blockFactor;
        setupBlockingList(symbolsToBlock);
        setupExcludedCommands(excludedCommand);
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
