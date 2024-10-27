package ru.overwrite.ublocker.blockgroups;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

@Getter
public class CommandGroup {

    private final String groupId;

    private final BlockType blockType;

    private final List<String> commandsToBlockString = new ArrayList<>();

    private final List<Pattern> commandsToBlockPattern = new ArrayList<>();

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    public CommandGroup(String groupId, BlockType blockType, List<String> commandsToBlock, List<Condition> conditionsToCheck, List<Action> actionsToExecute) {
        this.groupId = groupId;
        this.blockType = blockType;
        setupBlockingList(commandsToBlock);
        this.conditionsToCheck = conditionsToCheck;
        this.actionsToExecute = actionsToExecute;
    }

    private void setupBlockingList(List<String> commandsToBlock) {
        switch (this.blockType) {
            case STRING: {
                for (String s : commandsToBlock) {
                    this.commandsToBlockString.add(s.toLowerCase());
                }
                break;
            }
            case PATTERN: {
                for (String s : commandsToBlock) {
                    Pattern pattern = Pattern.compile(s);
                    this.commandsToBlockPattern.add(pattern);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

}
