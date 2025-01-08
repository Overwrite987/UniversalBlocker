package ru.overwrite.ublocker.blockgroups;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.conditions.Condition;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
public final class CommandGroup {

    private final String groupId;

    private final BlockType blockType;

    private Set<String> commandsToBlockString;

    private Set<Pattern> commandsToBlockPattern;

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
                Set<String> commandsToBlockString = new ObjectOpenHashSet<>(commandsToBlock.size());
                for (String s : commandsToBlock) {
                    commandsToBlockString.add(s.toLowerCase());
                }
                this.commandsToBlockString = ImmutableSet.copyOf(commandsToBlockString);
                break;
            }
            case PATTERN: {
                Set<Pattern> commandsToBlockPattern = new ObjectOpenHashSet<>(commandsToBlock.size());
                for (String s : commandsToBlock) {
                    Pattern pattern = Pattern.compile(s);
                    commandsToBlockPattern.add(pattern);
                }
                this.commandsToBlockPattern = ImmutableSet.copyOf(commandsToBlockPattern);
                break;
            }
            default: {
                break;
            }
        }
    }
}
