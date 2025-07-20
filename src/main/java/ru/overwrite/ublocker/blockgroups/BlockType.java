package ru.overwrite.ublocker.blockgroups;

public enum BlockType {

    STRING,
    PATTERN;

    public boolean isString() {
        return this == STRING;
    }

    public boolean isPattern() {
        return this == PATTERN;
    }

}
