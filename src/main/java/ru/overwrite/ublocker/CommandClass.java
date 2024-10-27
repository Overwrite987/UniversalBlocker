package ru.overwrite.ublocker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

public class CommandClass implements CommandExecutor {

    private final Main plugin;
    private final Config pluginConfig;

    public CommandClass(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String commandlabel, String[] args) {
        if (!sender.hasPermission("ublocker.admin")) {
            sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker §c§l" + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            long startTime = System.currentTimeMillis();
            plugin.reloadConfig();
            final FileConfiguration config = plugin.getConfig();
            final ConfigurationSection settings = config.getConfigurationSection("settings");
            Utils.SERIALIZER = settings.getString("serializer").toUpperCase();
            final String path = settings.getBoolean("custom_plugin_folder.enable")
                    ? settings.getString("custom_plugin_folder.path")
                    : plugin.getDataFolder().getAbsolutePath();
            plugin.setPath(path);
            if (settings.getBoolean("enable_chat_module")) {
                pluginConfig.setupChat(path);
            }
            if (settings.getBoolean("enable_symbol_module")) {
                pluginConfig.setupSymbols(path);
            }
            if (settings.getBoolean("enable_command_module")) {
                pluginConfig.setupCommands(path);
            }
            pluginConfig.setupExcluded(config);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.updateCommands();
            }
            long endTime = System.currentTimeMillis();
            sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена за §e" + (endTime - startTime) + " ms");
            return true;
        } else {
            sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker §c§l" + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
        }
        return false;
    }
}
