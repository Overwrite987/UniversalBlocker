package ru.overwrite.ublocker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.collect.ImmutableList;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.overwrite.ublocker.listeners.chat.*;
import ru.overwrite.ublocker.listeners.commands.*;
import ru.overwrite.ublocker.listeners.symbols.*;
import ru.overwrite.ublocker.task.*;
import ru.overwrite.ublocker.utils.*;
import ru.overwrite.ublocker.configuration.Config;

public final class Main extends JavaPlugin {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("'['dd-MM-yyyy']' HH:mm:ss -");

    @Setter
    private String path;

    @Getter
    private final Config pluginConfig = new Config(this);

    @Getter
    private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);

    @Getter
    private PluginMessage pluginMessage;

    private final Server server = getServer();

    private final List<String> incompatible = ImmutableList.of("ViaRewind", "NeroChat", "PermissionsEx", "AntiCmds");

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
        Utils.SERIALIZER = settings.getString("serializer").toUpperCase();
        path = settings.getBoolean("custom_plugin_folder.enable") ? settings.getString("custom_plugin_folder.path")
                : this.getDataFolder().getAbsolutePath();
        pluginConfig.setupExcluded(config);
        this.setupProxy(settings);
        if (settings.getBoolean("enable_chat_module")) {
            pluginConfig.setupChat(path);
            pm.registerEvents(new BanWords(this), this);
            pm.registerEvents(new BookChecker(this), this);
            pm.registerEvents(new ChatFilter(this), this);
            pm.registerEvents(new SignFilter(this), this);
            pm.registerEvents(new CommandFilter(this), this);
            pm.registerEvents(new NumbersCheck(this), this);
            pm.registerEvents(new CaseCheck(this), this); // Будет убрано в будущем, в связи с отсутствием необходимости
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
        if (settings.getBoolean("enable_metrics")) {
            new Metrics(this, 15379);
        }
        if (settings.getBoolean("update_checker")) {
            this.checkUpdates();
        }
        getCommand("universalblocker").setExecutor(new CommandClass(this));
        long endTime = System.currentTimeMillis();
        loggerInfo("Plugin started in " + (endTime - startTime) + " ms");
    }

    public boolean isPaper() { // Один хуй не сработает на 1.20+
        if (server.getName().equals("CraftBukkit")) {
            loggerInfo(" ");
            loggerInfo("§6============= §c! WARNING ! §6=============");
            loggerInfo("§eЭтот плагин работает только на Paper и его форках!");
            loggerInfo("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
            loggerInfo("§eСкачать Paper: §ahttps://papermc.io/downloads/all");
            loggerInfo("§6============= §c! WARNING ! §6=============");
            loggerInfo(" ");
            this.setEnabled(false);
            return false;
        }
        return true;
    }

    private boolean checkCompatible(PluginManager pm) {
        for (String inc : incompatible) {
            if (pm.isPluginEnabled(inc)) {
                loggerInfo(" ");
                loggerInfo("§c============= §6! WARNING ! §c=============");
                loggerInfo("§eНа сервере установлен плагин, который не совместим с §cUniversalBlocker!");
                loggerInfo("§eНазвание: §6" + inc);
                loggerInfo("§eУдалите данный плагин, для корректной работы §cUniversalBlocker!");
                loggerInfo("§c============= §6! WARNING ! §c=============");
                loggerInfo(" ");
                this.setEnabled(false);
                return false;
            }
        }
        return true;
    }

    private void checkUpdates() {
        Utils.checkUpdates(this, version -> {
            loggerInfo("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                loggerInfo("§aВы используете последнюю версию плагина!");
            } else {
                loggerInfo("§aВы используете устаревшую или некорректную версию плагина!");
                loggerInfo("§aВы можете загрузить последнюю версию плагина здесь:");
                loggerInfo("§bhttps://github.com/Overwrite987/UniversalBlocker/releases/latest");
            }
            loggerInfo("§6========================================");
        });
    }

    public void setupProxy(ConfigurationSection settings) {
        if (settings.getBoolean("proxy")) {
            server.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            pluginMessage = new PluginMessage(this);
            server.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", pluginMessage);
        }
    }

    public boolean isExcluded(Player p) {
        return !pluginConfig.excludedPlayers.isEmpty() && pluginConfig.excludedPlayers.contains(p.getName());
    }

    private void loggerInfo(String logMessage) {
        if (Utils.FOLIA) {
            getComponentLogger().info(LegacyComponentSerializer.legacySection().deserialize(logMessage));
        } else {
            getLogger().info(logMessage);
        }
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
        if (getConfig().getBoolean("settings.shutdown-on-disable")) {
            server.shutdown();
        }
    }
}
