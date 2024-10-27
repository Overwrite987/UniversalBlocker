package ru.overwrite.ublocker.actions;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public enum ActionType {

    HIDE,
    LITE_HIDE,
    BLOCK_TAB_COMPLETE,
    LITE_BLOCK_TAB_COMPLETE,
    BLOCK,
    LITE_BLOCK,
    BLOCK_ARGUMENTS,
    LITE_BLOCK_ARGUMENTS,
    BLOCK_CONSOLE,
    BLOCK_RCON,
    MESSAGE,
    ACTIONBAR,
    TITLE,
    SOUND,
    CONSOLE,
    NOTIFY,
    NOTIFY_SOUND,
    LOG;

    private static final Map<String, ActionType> BY_NAME = Stream.of(values()).collect(Collectors.toMap(Enum::name, en -> en));

    public static ActionType fromString(String str) {
        return BY_NAME.get(str.toUpperCase());
    }

}
