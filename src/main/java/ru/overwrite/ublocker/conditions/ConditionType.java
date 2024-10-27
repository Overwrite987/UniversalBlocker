package ru.overwrite.ublocker.conditions;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ConditionType {

    REGION,
    WORLD,
    GAMEMODE;

    private static final Map<String, ConditionType> BY_NAME = Stream.of(values()).collect(Collectors.toMap(Enum::name, en -> en));

    public static ConditionType fromString(String str) {
        return BY_NAME.get(str.toUpperCase());
    }

}
