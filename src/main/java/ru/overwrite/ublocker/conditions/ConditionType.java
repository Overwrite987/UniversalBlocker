package ru.overwrite.ublocker.conditions;

public enum ConditionType {

    REGION,
    WORLD,
    GAMEMODE,
    PLACEHOLDER;

    public static ConditionType get(String string) {
        try {
            return valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
