package ru.overwrite.ublocker.blockgroups;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public final class CommandGroup {

    private final String groupId;

    private final BlockType blockType;

    private final ObjectList<String> commandsToBlockString = new ObjectArrayList<>();

    private final ObjectList<Pattern> commandsToBlockPattern = new ObjectArrayList<>();

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    private final boolean blockAliases;

    public CommandGroup(String groupId,
                        BlockType blockType,
                        boolean blockAliases,
                        ObjectList<String> commandsToBlock,
                        List<Condition> conditionsToCheck,
                        List<Action> actionsToExecute) {
        this.groupId = groupId;
        this.blockType = blockType;
        this.blockAliases = blockAliases;
        this.setupBlockingList(commandsToBlock);
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
