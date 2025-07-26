package ru.overwrite.ublocker.listeners.symbols;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxBlocker extends SymbolBlocker {

    public SyntaxBlocker(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().toLowerCase();
        if (command.length() == 1) {
            return;
        }
        Player p = e.getPlayer();
        if (plugin.isExcluded(p)) {
            return;
        }
        outer:
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_SYMBOLS);
            if (group.blockFactor().isEmpty() || !group.blockFactor().contains("command")) {
                Utils.printDebug("Group " + group.groupId() + " does not have 'command' block factor. Skipping...", Utils.DEBUG_SYMBOLS);
                continue;
            }
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.conditionsToCheck())) {
                Utils.printDebug("Blocking does not fulfill the requirements. Skipping group...", Utils.DEBUG_SYMBOLS);
                continue;
            }
            switch (group.blockType()) {
                case STRING: {
                    if (checkStringBlock(e, p, command, group)) {
                        break outer;
                    }
                    break;
                }
                case PATTERN: {
                    if (checkPatternBlock(e, p, command, group)) {
                        break outer;
                    }
                    break;
                }
            }
        }
    }

    private boolean checkStringBlock(PlayerCommandPreprocessEvent e, Player p, String command, SymbolGroup group) {
        for (String symbol : group.symbolsToBlock()) {
            if (startWithExcludedString(command, group.excludedCommandsString())) {
                continue;
            }
            if (command.contains(symbol)) {
                Utils.printDebug("Command '" + command + "' contains blocked symbol" + symbol + ". (String)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, command, symbol, actions);
                return true;
            }
        }
        return false;
    }

    private boolean checkPatternBlock(PlayerCommandPreprocessEvent e, Player p, String command, SymbolGroup group) {
        for (Pattern pattern : group.patternsToBlock()) {
            Matcher matcher = pattern.matcher(command);
            if (startWithExcludedPattern(command, group.excludedCommandsPattern())) {
                continue;
            }
            if (matcher.find()) {
                Utils.printDebug("Command '" + command + "' contains blocked symbol" + matcher.group() + ". (Pattern)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, command, matcher.group(), actions);
                return true;
            }
        }
        return false;
    }

    private boolean startWithExcludedString(String command, List<String> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (String excluded : excludedList) {
            if (command.startsWith(excluded + " ")) {
                return true;
            }
        }
        return false;
    }

    private boolean startWithExcludedPattern(String command, List<Pattern> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (Pattern excluded : excludedList) {
            Matcher matcher = excluded.matcher(command);
            if (matcher.lookingAt()) {
                return true;
            }
        }
        return false;
    }
}