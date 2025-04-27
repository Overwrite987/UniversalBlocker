package ru.overwrite.ublocker;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.listeners.chat.*;
import ru.overwrite.ublocker.listeners.commands.*;
import ru.overwrite.ublocker.listeners.symbols.AnvilBlocker;
import ru.overwrite.ublocker.listeners.symbols.ChatBlocker;
import ru.overwrite.ublocker.listeners.symbols.SignBlocker;
import ru.overwrite.ublocker.listeners.symbols.SyntaxBlocker;
import ru.overwrite.ublocker.task.BukkitRunner;
import ru.overwrite.ublocker.task.PaperRunner;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.PluginMessage;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.utils.logging.BukkitLogger;
import ru.overwrite.ublocker.utils.logging.Logger;
import ru.overwrite.ublocker.utils.logging.PaperLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public final class UniversalBlocker extends JavaPlugin {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("'['dd-MM-yyyy']' HH:mm:ss -");

    @Getter(AccessLevel.NONE)
    private final Server server = getServer();

    private final Logger pluginLogger = Utils.FOLIA ? new PaperLogger(this) : new BukkitLogger(this);

    private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);

    private final Config pluginConfig = new Config(this);

    @Setter
    private String path;

    private PluginMessage pluginMessage;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        if (!isPaper()) {
            return;
        }
        PluginManager pm = server.getPluginManager();
        if (!checkCompatible(pm)) {
            return;
        }
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        final ConfigurationSection settings = config.getConfigurationSection("settings");
        Utils.setupColorizer(settings);
        path = settings.getBoolean("custom_plugin_folder.enable") ? settings.getString("custom_plugin_folder.path")
                : this.getDataFolder().getAbsolutePath();
        pluginConfig.setupExcluded(config);
        this.setupProxy(settings);
        this.registerEvents(pm, settings);
        if (settings.getBoolean("enable_metrics")) {
            new Metrics(this, 15379);
        }
        if (settings.getBoolean("update_checker")) {
            this.checkUpdates();
        }
        getCommand("universalblocker").setExecutor(new CommandClass(this));
        long endTime = System.currentTimeMillis();
        pluginLogger.info("Plugin started in " + (endTime - startTime) + " ms");
    }

    public boolean isPaper() { // Один хуй не сработает на 1.20+
        if (server.getName().equals("CraftBukkit")) {
            pluginLogger.info(" ");
            pluginLogger.info("§6============= §c! WARNING ! §6=============");
            pluginLogger.info("§eЭтот плагин работает только на Paper и его форках!");
            pluginLogger.info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
            pluginLogger.info("§eСкачать Paper: §ahttps://papermc.io/downloads/all");
            pluginLogger.info("§6============= §c! WARNING ! §6=============");
            pluginLogger.info(" ");
            this.setEnabled(false);
            return false;
        }
        return true;
    }

    private boolean checkCompatible(PluginManager pm) {
        for (String inc : ImmutableList.of("ViaRewind", "NeroChat", "PermissionsEx", "AntiCmds")) {
            if (pm.isPluginEnabled(inc)) {
                pluginLogger.info(" ");
                pluginLogger.info("§c============= §6! WARNING ! §c=============");
                pluginLogger.info("§eНа сервере установлен плагин, который не совместим с §cUniversalBlocker!");
                pluginLogger.info("§eНазвание: §6" + inc);
                pluginLogger.info("§eУдалите данный плагин, для корректной работы §cUniversalBlocker!");
                pluginLogger.info("§c============= §6! WARNING ! §c=============");
                pluginLogger.info(" ");
                this.setEnabled(false);
                return false;
            }
        }
        return true;
    }

    private void checkUpdates() {
        Utils.checkUpdates(this, version -> {
            pluginLogger.info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                pluginLogger.info("§aВы используете последнюю версию плагина!");
            } else {
                pluginLogger.info("§aВы используете устаревшую или некорректную версию плагина!");
                pluginLogger.info("§aВы можете загрузить последнюю версию плагина здесь:");
                pluginLogger.info("§bhttps://github.com/Overwrite987/UniversalBlocker/releases/latest");
            }
            pluginLogger.info("§6========================================");
        });
    }

    private void setupProxy(ConfigurationSection settings) {
        if (settings.getBoolean("proxy")) {
            Messenger messenger = server.getMessenger();
            messenger.registerOutgoingPluginChannel(this, "BungeeCord");
            pluginMessage = new PluginMessage(this);
            messenger.registerIncomingPluginChannel(this, "BungeeCord", pluginMessage);
        }
    }

    public void registerEvents(PluginManager pm, ConfigurationSection settings) {
        if (settings.getBoolean("enable_chat_module")) {
            pluginConfig.setupChat(path);
            if (pluginConfig.getChatCharsSettings() != null) {
                pm.registerEvents(new ChatFilter(this), this);
            }
            if (pluginConfig.getBookCharsSettings() != null) {
                pm.registerEvents(new BookChecker(this), this);
            }
            if (pluginConfig.getSignCharsSettings() != null) {
                pm.registerEvents(new SignFilter(this), this);
            }
            if (pluginConfig.getCommandCharsSettings() != null) {
                pm.registerEvents(new CommandFilter(this), this);
            }
            if (pluginConfig.getNumberCheckSettings() != null) {
                pm.registerEvents(new NumbersCheck(this), this);
            }
            if (pluginConfig.getCaseCheckSettings() != null) {
                pm.registerEvents(new CaseCheck(this), this);
            }
            if (pluginConfig.getSameMessagesSettings() != null) {
                pm.registerEvents(new SameMessageLimiter(this), this);
            }
            if (pluginConfig.getBanWordsSettings() != null) {
                pm.registerEvents(new BanWords(this), this);
            }
        }
        if (settings.getBoolean("enable_symbol_module")) {
            pluginConfig.setupSymbols(path);
            pm.registerEvents(new SyntaxBlocker(this), this);
            pm.registerEvents(new ChatBlocker(this), this);
            pm.registerEvents(new SignBlocker(this), this);
            pm.registerEvents(new AnvilBlocker(this), this);
        }
        if (settings.getBoolean("enable_command_module")) {
            pluginConfig.setupCommands(path);
            pm.registerEvents(new CommandBlocker(this), this);
            pm.registerEvents(new ConsoleBlocker(this), this);
            pm.registerEvents(new RconBlocker(this), this);
            pm.registerEvents(new TabComplete(this), this);
            pm.registerEvents(new CommandHider(this), this);
        }
    }

    public boolean isAdmin(Player player, String permission) {
        return (player.hasPermission(permission) || isExcluded(player));
    }

    public boolean isExcluded(Player p) {
        return !pluginConfig.getExcludedPlayers().isEmpty() && pluginConfig.getExcludedPlayers().contains(p.getName());
    }

    public void logAction(String key, String fileName) {
        runner.runAsync(() -> logToFile(key.replace("%date%", LocalDateTime.now().format(TIME_FORMATTER)), fileName));
    }

    public void logToFile(String message, String fileName) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(path, fileName), true))) {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        runner.cancelTasks();
        if (getConfig().getBoolean("settings.shutdown_on_disable")) {
            server.shutdown();
        }
        if (pluginMessage != null) {
            Messenger messenger = server.getMessenger();
            messenger.unregisterOutgoingPluginChannel(this);
            messenger.unregisterIncomingPluginChannel(this);
        }
    }
}
