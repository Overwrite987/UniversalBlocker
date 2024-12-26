package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.regex.Pattern;

@Getter
public final class CommandGroup {

    private final String groupId;

    private final BlockType blockType;

    private List<String> commandsToBlockString;

    private List<Pattern> commandsToBlockPattern;

    private final List<Condition> conditionsToCheck;

    private final List<Action> actionsToExecute;

    private final boolean blockAliases;

    public CommandGroup(String groupId,
                        BlockType blockType,
                        boolean blockAliases,
                        List<String> commandsToBlock,
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
                List<String> commandsToBlockString = new ObjectArrayList<>(commandsToBlock.size());
                for (String s : commandsToBlock) {
                    commandsToBlockString.add(s.toLowerCase());
                }
                this.commandsToBlockString = ImmutableList.copyOf(commandsToBlockString);
                break;
            }
            case PATTERN: {
                List<Pattern> commandsToBlockPattern = new ObjectArrayList<>(commandsToBlock.size());
                for (String s : commandsToBlock) {
                    Pattern pattern = Pattern.compile(s);
                    commandsToBlockPattern.add(pattern);
                }
                this.commandsToBlockPattern = ImmutableList.copyOf(commandsToBlockPattern);
                break;
            }
            default: {
                break;
            }
        }
    }
}
