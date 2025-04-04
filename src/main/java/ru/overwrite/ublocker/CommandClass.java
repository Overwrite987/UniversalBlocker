package ru.overwrite.ublocker;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.listeners.commands.ConsoleBlocker;
import ru.overwrite.ublocker.listeners.commands.RconBlocker;
import ru.overwrite.ublocker.utils.Utils;

public class CommandClass implements CommandExecutor {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public CommandClass(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("ublocker.admin")) {
            sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker §c§l" + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§6/" + label + " reload - перезагрузить конфиг");
            return true;
        }
        switch (args[0]) {
            case "reload": {
                long startTime = System.currentTimeMillis();
                plugin.getRunner().cancelTasks();
                HandlerList.unregisterAll(plugin);
                plugin.reloadConfig();
                final FileConfiguration config = plugin.getConfig();
                final ConfigurationSection settings = config.getConfigurationSection("settings");
                Utils.setupColorizer(settings);
                String path = settings.getBoolean("custom_plugin_folder.enable")
                        ? settings.getString("custom_plugin_folder.path")
                        : plugin.getDataFolder().getAbsolutePath();
                plugin.setPath(path);
                pluginConfig.setupExcluded(config);
                plugin.registerEvents(Bukkit.getPluginManager(), settings);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.updateCommands();
                }
                long endTime = System.currentTimeMillis();
                sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена за §e" + (endTime - startTime) + " ms");
                return true;
            }
            case "debug": {
                Utils.DEBUG = !Utils.DEBUG;
                String message = "§cUniversalBlocker §7> §6Дебаг переключен в значение: "
                        + (Utils.DEBUG ? "§a" : "§c")
                        + Utils.DEBUG;
                sender.sendMessage(message);
                return true;
            }
            case "lockcommands": {
                ConsoleBlocker.FULL_LOCK = !ConsoleBlocker.FULL_LOCK;
                String message = "§cUniversalBlocker §7> §6Блокировка команд игроков переключена в значение: "
                        + (ConsoleBlocker.FULL_LOCK ? "§a" : "§c")
                        + ConsoleBlocker.FULL_LOCK;
                sender.sendMessage(message);
                return true;
            }
            case "lockconsole": {
                ConsoleBlocker.FULL_LOCK = !ConsoleBlocker.FULL_LOCK;
                String message = "§cUniversalBlocker §7> §6Блокировка команд консоли переключена в значение: "
                        + (ConsoleBlocker.FULL_LOCK ? "§a" : "§c")
                        + ConsoleBlocker.FULL_LOCK;
                sender.sendMessage(message);
                return true;
            }
            case "lockrcon": {
                RconBlocker.FULL_LOCK = !RconBlocker.FULL_LOCK;
                String message = "§cUniversalBlocker §7> §6Блокировка команд ркона переключена в значение: "
                        + (RconBlocker.FULL_LOCK ? "§a" : "§c")
                        + RconBlocker.FULL_LOCK;
                sender.sendMessage(message);
                return true;
            }
        }
        sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker §c§l" + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
        return true;
    }
}
