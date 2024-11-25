package ru.overwrite.ublocker.utils.logging;

import ru.overwrite.ublocker.UniversalBlocker;

public class BukkitLogger implements Logger {

    private final UniversalBlocker plugin;

    public BukkitLogger(UniversalBlocker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String msg) {
        plugin.getLogger().info(msg);
    }

    @Override
    public void warn(String msg) {
        plugin.getLogger().warning(msg);
    }

}
