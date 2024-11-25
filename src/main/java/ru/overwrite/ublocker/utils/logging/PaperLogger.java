package ru.overwrite.ublocker.utils.logging;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.overwrite.ublocker.UniversalBlocker;

public class PaperLogger implements Logger {

    private final UniversalBlocker plugin;

    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public PaperLogger(UniversalBlocker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getComponentLogger().info(legacySection.deserialize(msg));
    }

    @Override
    public void warn(String msg) {
        plugin.getComponentLogger().warn(legacySection.deserialize(msg));
    }

}
