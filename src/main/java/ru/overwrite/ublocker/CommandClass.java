package ru.overwrite.ublocker;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.listeners.chat.ChatListener;
import ru.overwrite.ublocker.listeners.commands.ConsoleBlocker;
import ru.overwrite.ublocker.listeners.commands.RconBlocker;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

public class CommandClass implements TabExecutor {

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
            sender.sendMessage("§6/" + label + " debug <chat|commands|symbols> - включить дебаг");
            sender.sendMessage("§6/" + label + " lock <commands|console|rcon> - отключить выбранный тип команд");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload": {
                reloadPlugin(sender);
                return true;
            }
            case "debug": {
                if (args.length < 2) {
                    sender.sendMessage("§cUniversalBlocker §7> §6Использование: /" + label + " debug <chat|commands|symbols>");
                    return true;
                }
                String debugType = args[1].toLowerCase();
                switch (debugType) {
                    case "chat" ->
                            toggleFeature("Дебаг для", "чата", Utils.DEBUG_CHAT, v -> Utils.DEBUG_CHAT = v, sender);
                    case "commands" ->
                            toggleFeature("Дебаг для", "команд", Utils.DEBUG_COMMANDS, v -> Utils.DEBUG_COMMANDS = v, sender);
                    case "symbols" ->
                            toggleFeature("Дебаг для", "символов", Utils.DEBUG_SYMBOLS, v -> Utils.DEBUG_SYMBOLS = v, sender);
                    case "printknowncommands" ->
                            plugin.getPluginLogger().info("Known commands: " + new TreeSet<>(Bukkit.getCommandMap().getKnownCommands().keySet()));
                    case "printcommandgroups" ->
                            plugin.getPluginLogger().info("Known command groups: " + pluginConfig.getCommandBlockGroupSet());
                    case "printsymbolgroups" ->
                            plugin.getPluginLogger().info("Known command groups: " + pluginConfig.getSymbolBlockGroupSet());
                    default -> {
                        sender.sendMessage("§cUniversalBlocker §7> §6Неизвестный тип дебага! Доступные: chat, commands, symbols");
                        return true;
                    }
                }
                return true;
            }
            case "lock": {
                if (args.length < 2) {
                    sender.sendMessage("§cUniversalBlocker §7> §6Использование: /" + label + " lock <commands|console|rcon>");
                    return true;
                }
                String lockType = args[1].toLowerCase();
                switch (lockType) {
                    case "commands" ->
                            toggleFeature("Блокировка команд для", "игроков", ConsoleBlocker.FULL_LOCK, v -> ConsoleBlocker.FULL_LOCK = v, sender);
                    case "console" ->
                            toggleFeature("Блокировка команд для", "консоли", ConsoleBlocker.FULL_LOCK, v -> ConsoleBlocker.FULL_LOCK = v, sender);
                    case "rcon" ->
                            toggleFeature("Блокировка команд для", "ркона", RconBlocker.FULL_LOCK, v -> RconBlocker.FULL_LOCK = v, sender);
                    default -> {
                        sender.sendMessage("§cUniversalBlocker §7> §6Неизвестный тип блокировки! Доступные: commands, console, rcon");
                        return false;
                    }
                }
                return true;
            }
        }
        sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker §c§l" + plugin.getDescription().getVersion() + "§7 by §5OverwriteMC");
        return true;
    }

    private void toggleFeature(String actionWord, String featureName, boolean currentValue, Consumer<Boolean> setter, CommandSender sender) {
        boolean newValue = !currentValue;
        setter.accept(newValue);

        String color = newValue ? "§a" : "§c";
        String message = "§cUniversalBlocker §7> §6" + actionWord + " " + featureName + " переключен в значение: " + color + newValue;

        sender.sendMessage(message);
    }

    private void reloadPlugin(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        plugin.getRunner().cancelTasks();
        for (ChatListener listener : plugin.getChatListeners().values()) {
            listener.setRegistered(false);
        }
        HandlerList.unregisterAll(plugin);
        plugin.reloadConfig();
        final FileConfiguration config = plugin.getConfig();
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        Utils.setupColorizer(settings);
        plugin.setupPath(settings);
        pluginConfig.setupExcluded(config);
        plugin.registerEvents(Bukkit.getPluginManager(), settings);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.updateCommands();
        }
        long endTime = System.currentTimeMillis();
        sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена за §e" + (endTime - startTime) + " ms");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.hasPermission("rtp.admin")) {
            return List.of();
        }
        final List<String> completions = new ObjectArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("debug");
            completions.add("lock");
        }
        switch (args[0].toLowerCase()) {
            case "debug": {
                if (args.length == 2) {
                    completions.add("chat");
                    completions.add("commands");
                    completions.add("symbols");
                    completions.add("printknowncommands");
                    completions.add("printcommandgroups");
                    completions.add("printsymbolgroups");
                }
                break;
            }
            case "lock": {
                if (args.length == 2) {
                    completions.add("commands");
                    completions.add("console");
                    completions.add("rcon");
                }
                break;
            }
            default: {
                break;
            }
        }
        return getResult(args, completions);
    }

    private List<String> getResult(String[] args, List<String> completions) {
        if (completions.isEmpty()) {
            return completions;
        }
        final List<String> result = new ObjectArrayList<>();
        for (int i = 0; i < completions.size(); i++) {
            String c = completions.get(i);
            if (StringUtil.startsWithIgnoreCase(c, args[args.length - 1])) {
                result.add(c);
            }
        }
        return result;
    }
}
