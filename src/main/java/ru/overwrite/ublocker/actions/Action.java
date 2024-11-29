package ru.overwrite.ublocker.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Action(ActionType type, String context) {

    private static final Pattern ACTION_PATTERN = Pattern.compile("\\[(\\w+)] ?(.*)");

    public static Action fromString(String str) {
        Matcher matcher = ACTION_PATTERN.matcher(str);
        if (!matcher.matches()) return null;
        ActionType type = ActionType.fromString(matcher.group(1));
        if (type == null) return null;
        return new Action(type, matcher.group(2).trim());
    }

}
