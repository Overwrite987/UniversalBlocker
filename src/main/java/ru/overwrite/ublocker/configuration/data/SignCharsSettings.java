package ru.overwrite.ublocker.configuration.data;

import it.unimi.dsi.fastutil.chars.CharSet;
import ru.overwrite.ublocker.blockgroups.BlockType;

import java.util.regex.Pattern;

public record SignCharsSettings(
        BlockType mode,
        CharSet charSet,
        Pattern pattern,
        CancellationSettings cancellationSettings
) {
}
