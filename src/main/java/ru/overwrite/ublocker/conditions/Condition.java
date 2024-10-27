package ru.overwrite.ublocker.conditions;


import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record Condition(ConditionType type, String operator, String context) {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[(\\w+)](?: ?(.*))");

    public static Condition fromString(String str) {
        Matcher matcher = ACTION_PATTERN.matcher(str);
        if (!matcher.matches()) return null;
        ConditionType type = ConditionType.fromString(matcher.group(1));
        if (type == null) return null;
        String[] globalContext = matcher.group(2).split(" ");
        return new Condition(type, globalContext[0].trim(), globalContext[1].trim());
    }

}
