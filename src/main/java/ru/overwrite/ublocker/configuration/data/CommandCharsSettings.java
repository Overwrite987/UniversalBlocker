package ru.overwrite.ublocker.configuration.data;

import it.unimi.dsi.fastutil.chars.CharSet;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.List;
import java.util.regex.Pattern;

public record CommandCharsSettings(
        BlockType mode,
        CharSet charSet,
        Pattern pattern,
        List<Action> actionsToExecute
) {
}
