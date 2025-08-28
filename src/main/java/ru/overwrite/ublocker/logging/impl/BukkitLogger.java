package ru.overwrite.ublocker.logging.impl;

import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.logging.Logger;

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
