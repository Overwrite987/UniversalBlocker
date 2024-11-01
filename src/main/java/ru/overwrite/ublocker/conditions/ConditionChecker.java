package ru.overwrite.ublocker.conditions;

import java.util.List;

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

    public static boolean isMeetsRequirements(Player p, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        for (Condition condition : conditions) {
            final String context = condition.context();
            final String operator = condition.operator();

            switch (condition.type()) {
                // Дальше идет типа говнокод, потом переделаю
                case REGION:
                    if (!hasWorldGuard()) return false;

                    List<String> regions = WGUtils.getRegions(p.getLocation());
                    boolean containsRegion = regions.contains(context);

                    if ((operator.equals("==") && !containsRegion) ||
                            (operator.equals("!=") && containsRegion)) {
                        return false;
                    }
                    break;

                case WORLD:
                    String worldName = p.getWorld().getName();

                    if ((operator.equals("==") && !worldName.equals(context)) ||
                            (operator.equals("!=") && worldName.equals(context))) {
                        return false;
                    }
                    break;

                case GAMEMODE:
                    GameMode playerMode = p.getGameMode();
                    GameMode conditionMode = GameMode.valueOf(context);

                    if ((operator.equals("==") && playerMode != conditionMode) ||
                            (operator.equals("!=") && playerMode == conditionMode)) {
                        return false;
                    }
                    break;

                default:
                    break;
            }
        }
        return true;
    }

}
