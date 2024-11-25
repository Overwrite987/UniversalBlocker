package ru.overwrite.ublocker.conditions;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import ru.overwrite.ublocker.utils.WGUtils;

public class ConditionChecker {

    private static Boolean hasWorldGuard = null;

    public static boolean hasWorldGuard() {
        if (hasWorldGuard == null) {
            try {
                Class.forName("com.sk89q.worldguard.protection.flags.registry.FlagConflictException");
                hasWorldGuard = true;
            } catch (ClassNotFoundException ex) {
                hasWorldGuard = false;
            }
        }
        return hasWorldGuard;
    }

    public static boolean isMeetsRequirements(Player p, ObjectList<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (Condition condition : conditions) {
            final String context = condition.context();
            final String operator = condition.operator();

            boolean meetsCondition;
            switch (condition.type()) {
                case REGION: {
                    if (!hasWorldGuard()) return false;
                    ObjectList<String> regions = WGUtils.getRegions(p.getLocation());
                    meetsCondition = evaluateCondition(operator, regions.contains(context));
                    break;
                }
                case WORLD: {
                    String worldName = p.getWorld().getName();
                    meetsCondition = evaluateCondition(operator, worldName.equals(context));
                    break;
                }
                case GAMEMODE: {
                    GameMode playerMode = p.getGameMode();
                    GameMode conditionMode = GameMode.valueOf(context);
                    meetsCondition = evaluateCondition(operator, playerMode == conditionMode);
                    break;
                }
                default: {
                    continue;
                }
            }

            if (!meetsCondition) {
                return false;
            }
        }
        return true;
    }

    private static boolean evaluateCondition(String operator, boolean conditionMet) {
        return switch (operator) {
            case "==" -> conditionMet;
            case "!=" -> !conditionMet;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

}
