package ru.overwrite.ublocker.actions;

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

    public static ActionType get(String string) {
        try {
            return valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
