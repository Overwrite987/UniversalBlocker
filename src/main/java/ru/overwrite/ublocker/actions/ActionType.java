package ru.overwrite.ublocker.actions;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
    NOTIFY_CONSOLE,
    NOTIFY_SOUND,
    LOG;

    private static final Object2ObjectMap<String, ActionType> BY_NAME = new Object2ObjectOpenHashMap<>();

    static {
        for (ActionType actionType : values()) {
            BY_NAME.put(actionType.name(), actionType);
        }
    }

    public static ActionType fromString(String str) {
        return BY_NAME.get(str.toUpperCase());
    }

}
