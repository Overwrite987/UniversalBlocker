package ru.overwrite.ublocker;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
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
import ru.overwrite.ublocker.listeners.symbols.*;
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
import java.util.Map;

@Getter
public final class UniversalBlocker extends JavaPlugin {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("'['dd-MM-yyyy']' HH:mm:ss -");

    @Getter(AccessLevel.NONE)
    private final Server server = getServer();

    private final Logger pluginLogger = Utils.FOLIA ? new PaperLogger(this) : new BukkitLogger(this);

    private final Runner runner = Utils.FOLIA ? new PaperRunner(this) : new BukkitRunner(this);

    private final Config pluginConfig = new Config(this);

    private final Reference2ObjectMap<String, ChatListener> chatListeners = new Reference2ObjectOpenHashMap<>();

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
        Utils.IGNORE_UNKNOWN_COMMANDS = settings.getBoolean("ignore_unknown_commands");
        this.setupPlaceholders(settings, getServer().getPluginManager());
        this.setupPath(settings);
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
        for (String inc : new String[]{"ViaRewind", "PermissionsEx", "AntiCmds"}) {
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

    public void setupPlaceholders(ConfigurationSection settings, PluginManager pluginManager) {
        if (!settings.getBoolean("papi_support", true) || !pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return;
        }
        Utils.USE_PAPI = true;
        pluginLogger.info("§eПлейсхолдеры подключены!");
    }

    public void setupPath(ConfigurationSection settings) {
        ConfigurationSection customPluginFolder = settings.getConfigurationSection("custom_plugin_folder");
        this.path = customPluginFolder.getBoolean("enable") ? customPluginFolder.getString("path") : this.getDataFolder().getAbsolutePath();
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
            Reference2ObjectMap<String, ChatListener> chatListeners = getChatListeners();
            pluginConfig.setupChat(path);
            for (Map.Entry<String, ChatListener> entry : chatListeners.entrySet()) {
                ChatListener listener = entry.getValue();
                if (listener.isRegistered()) {
                    pm.registerEvents(listener, this);
                    Utils.printDebug("Registered " + entry.getKey() + " chat check", Utils.DEBUG_CHAT);
                }
            }
        }
        if (settings.getBoolean("enable_symbol_module")) {
            pluginConfig.setupSymbols(path);
            pm.registerEvents(new SyntaxBlocker(this), this);
            pm.registerEvents(new ChatBlocker(this), this);
            pm.registerEvents(new ConsoleSymbolBlocker(this), this);
            pm.registerEvents(new RconSymbolBlocker(this), this);
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

    public Reference2ObjectMap<String, ChatListener> getChatListeners() {
        if (chatListeners.isEmpty()) {
            chatListeners.put(ChatFilter.class.getSimpleName(), new ChatFilter(this));
            chatListeners.put(BookFilter.class.getSimpleName(), new BookFilter(this));
            chatListeners.put(SignFilter.class.getSimpleName(), new SignFilter(this));
            chatListeners.put(CommandFilter.class.getSimpleName(), new CommandFilter(this));
            chatListeners.put(NumbersCheck.class.getSimpleName(), new NumbersCheck(this));
            chatListeners.put(CaseCheck.class.getSimpleName(), new CaseCheck(this));
            chatListeners.put(SameMessageLimiter.class.getSimpleName(), new SameMessageLimiter(this));
            chatListeners.put(BanWords.class.getSimpleName(), new BanWords(this));
        }
        return chatListeners;
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
