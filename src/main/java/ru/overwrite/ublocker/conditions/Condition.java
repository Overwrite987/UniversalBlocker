package ru.overwrite.ublocker.conditions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Condition(ConditionType type, String operator, String context) {

    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\[(\\w+)] ?(.*)");

    public static Condition fromString(String str) {
        Matcher matcher = CONDITION_PATTERN.matcher(str);
        if (!matcher.matches()) return null;
        ConditionType type = ConditionType.get(matcher.group(1));
        if (type == null) return null;
        String[] globalContext = matcher.group(2).split(" ");
        return new Condition(type, globalContext[0].trim(), globalContext[1].trim());
    }

}
