package ru.overwrite.ublocker.listeners.symbols;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnvilBlocker extends SymbolBlocker {

    public AnvilBlocker(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.ANVIL || e.getSlot() != 2) {
            return;
        }
        ItemStack resultItem = e.getCurrentItem();
        if (resultItem == null || !resultItem.hasItemMeta() || !resultItem.getItemMeta().hasDisplayName()) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        if (plugin.isExcluded(p)) {
            return;
        }
        String name = resultItem.getItemMeta().getDisplayName();
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_SYMBOLS);
            if (group.blockFactor().isEmpty() || !group.blockFactor().contains("anvil")) {
                Utils.printDebug("Group " + group.groupId() + " does not have 'anvil' block factor. Skipping...", Utils.DEBUG_SYMBOLS);
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
                    checkStringBlock(e, p, name, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, name, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(InventoryClickEvent e, Player p, String name, SymbolGroup group) {
        for (String symbol : group.symbolsToBlock()) {
            if (name.contains(symbol)) {
                Utils.printDebug("Item name '" + name + "' contains blocked symbol" + symbol + ". (String)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, name, symbol, actions);
            }
        }
    }

    private void checkPatternBlock(InventoryClickEvent e, Player p, String name, SymbolGroup group) {
        for (Pattern pattern : group.patternsToBlock()) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                Utils.printDebug("Item name '" + name + "' contains blocked symbol" + matcher.group() + ". (Pattern)", Utils.DEBUG_SYMBOLS);
                List<Action> actions = group.actionsToExecute();
                super.executeActions(e, p, name, matcher.group(), actions);
            }
        }
    }
}