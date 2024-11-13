package ru.overwrite.ublocker.conditions;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public enum ConditionType {

    REGION,
    WORLD,
    GAMEMODE;

    private static final Object2ObjectMap<String, ConditionType> BY_NAME = new Object2ObjectOpenHashMap<>();

    static {
        for (ConditionType conditionType : values()) {
            BY_NAME.put(conditionType.name(), conditionType);
        }
    }

    public static ConditionType fromString(String str) {
        return BY_NAME.get(str.toUpperCase());
    }

}
