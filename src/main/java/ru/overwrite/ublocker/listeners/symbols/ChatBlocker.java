package ru.overwrite.ublocker.listeners.symbols;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatBlocker extends SymbolBlocker {

    public ChatBlocker(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p)) {
            return;
        }
        String message = e.getMessage().toLowerCase();
        outer:
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_SYMBOLS);
            if (group.blockFactor().isEmpty() || !group.blockFactor().contains("chat")) {
                Utils.printDebug("Group " + group.groupId() + " does not have 'chat' block factor. Skipping...", Utils.DEBUG_SYMBOLS);
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
                    if (checkStringBlock(e, p, message, group)) {
                        break outer;
                    }
                    break;
                }
                case PATTERN: {
                    if (checkPatternBlock(e, p, message, group)) {
                        break outer;
                    }
                    break;
                }
            }
        }
    }

    private boolean checkStringBlock(AsyncPlayerChatEvent e, Player p, String message, SymbolGroup group) {
        for (String symbol : group.symbolsToBlock()) {
            if (message.contains(symbol)) {
                Utils.printDebug("Message '" + message + "' contains blocked symbol" + symbol + ". (String)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, message, symbol, actions);
                return true;
            }
        }
        return false;
    }

    private boolean checkPatternBlock(AsyncPlayerChatEvent e, Player p, String message, SymbolGroup group) {
        for (Pattern pattern : group.patternsToBlock()) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                Utils.printDebug("Message '" + message + "' contains blocked symbol" + matcher.group() + ". (Pattern)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, message, matcher.group(), actions);
                return true;
            }
        }
        return false;
    }
}
