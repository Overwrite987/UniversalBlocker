package ru.Overwrite.noCmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;
import java.util.function.Consumer;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.noCmd.listeners.*;
import ru.Overwrite.noCmd.utils.*;

public class Main extends JavaPlugin {
	
  Server server = getServer();
  
  public boolean debug, folia;
  private final PluginManager pluginManager = server.getPluginManager();
  public final Logger logger = getLogger();
	
  private static Main instance;
	  
  public static Main getInstance() {
	  return instance;
  }
 
  public final int SUB_VERSION = Integer.parseInt(
		  server.getClass().getPackage().getName()
          		.replace(".", ",")
          		.split(",")[3]
          			.replace("1_", "")
          			.replaceAll("_R\\d", "")
          			.replace("v", "")
  );
  
  public void onEnable() {
	checkPaper();
	long startTime = System.currentTimeMillis();
	instance = this;
    getCommand("universalblocker").setExecutor(new CommandClass());
    saveDefaultConfig();
    FileConfiguration config = getConfig();
    if (config.getBoolean("settings.debug")) {
    	debug = true;
    }
    Config.loadBooleans(config);
    Config.loadMessageFile();
    Config.loadMessages();
    Config.loadNotifies();
    Config.loadTitles();
    Config.setupExcluded(config);
    if (config.getBoolean("settings.enable-sounds")) {
    	Config.setupSounds(config);
    }
    if (config.getBoolean("settings.enable-metrics")) {
      new Metrics(this, 15379);
    }
    if (config.getBoolean("settings.update-checker") && !folia) {
    	checkUpdates(this, version -> {
            logger.info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                logger.info("§aВы используете последнюю версию плагина!");
            } else {
                logger.info("§aВы используете устаревшую или некорректную версию плагина!");
                logger.info("§aВы можете загрузить последнюю версию плагина здесь:");
                logger.info("§bhttps://github.com/Overwrite987/UniversalBlocker/releases/");
            }
            logger.info("§6========================================");
        });
    }
    if (config.getBoolean("settings.enable-blocksyntax")) {
    	pluginManager.registerEvents(new BlockSyntax(), this);
    	if (debug) {
    		logger.info("§6> BlockSyntax - enabled");
        }
    }
    if (config.getBoolean("settings.enable-words-blocker")) {
    	Config.setupBanWords(config);
    	pluginManager.registerEvents(new BanWords(), this);
    	if (debug) {
        	logger.info("§6> BanWords - enabled");
        }
    }
    if (config.getBoolean("settings.enable-allowed-chars")) {
    	Config.setupChars(config);
    	pluginManager.registerEvents(new ChatFilter(), this);
    	if (debug) {
        	logger.info("§6> ChatFilter - enabled");
        }
    }
    if (config.getBoolean("settings.enable-command-blocker")) {
        Config.setupCommands(config);
        pluginManager.registerEvents(new CommandBlocker(), this);
        if (debug) {
        	logger.info("§6> CommandBlocker - enabled");
        }
    }
    if (SUB_VERSION >= 13 && getConfig().getBoolean("settings.hide-blocked-commands-from-tab-comple")) {
    	if (Config.liteblocked.isEmpty() && Config.fullblocked.isEmpty()) {
        	Config.setupCommands(config);
        }
    	pluginManager.registerEvents(new CommandHider(), this);
        if (debug) {
        	logger.info("§6> CommandHider - enabled");
        }
    } else {
    	logger.info("Скрытие из таб-комплита не доступно на вашей версии!");
    }
    if (config.getBoolean("settings.enable-console-blocker")) {
    	pluginManager.registerEvents(new ConsoleBlocker(), this);
    	Config.setupConsole(config);
    	if (debug) {
        	logger.info("§6> ConsoleBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-numbers-check")) {
    	pluginManager.registerEvents(new NumbersCheck(), this);
    	if (debug) {
        	logger.info("§6> NumbersCheck - enabled");
        }
    }
    if (config.getBoolean("settings.enable-rcon-blocker")) {
    	pluginManager.registerEvents(new RconBlocker(), this);
    	Config.setupRcon(config);
    	if (debug) {
        	logger.info("§6> RconBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-symbol-blocker")) {
    	Config.setupSyntax(config);
    	pluginManager.registerEvents(new SyntaxBlocker(), this);
    	if (debug) {
        	logger.info("§6> SyntaxBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-sign-symbol-blocker")) {
    	Config.setupSignSyntax(config);
    	pluginManager.registerEvents(new SignSymbolBlocker(), this);
    	if (debug) {
        	logger.info("§6> SignSymbolBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-tab-complere-blocker")) {
    	Config.setupArgshidden(config);
    	pluginManager.registerEvents(new TabComplete(), this);
    	if (debug) {
        	logger.info("§6> TabComplete - enabled");
        }
    }
    long endTime = System.currentTimeMillis();
    logger.info("Plugin started in " + (endTime - startTime) + " ms");
  }
  
  private void checkPaper() {
  	if (server.getName().equals("CraftBukkit")) {
          logger.info("§6============= §6! WARNING ! §c=============");
          logger.info("§eЭтот плагин работает только на Paper и его форках!");
          logger.info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
          logger.info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
          logger.info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
          logger.info("§6============= §6! WARNING ! §c=============");
          setEnabled(false);
      } else if (server.getName().equals("Folia")) {
    	  logger.info("Активируем поддержку Folia!");
    	  folia = true;
      }
  }
  
  private void checkUpdates(Plugin plugin, Consumer<String> consumer) {
	  server.getScheduler().runTaskAsynchronously(plugin, () -> {
	      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION").openStream()))) {
	    	  String version = reader.readLine();
	          if (version != null) {
	              consumer.accept(version.trim());
	          }
	      } catch (IOException exception) {
	          logger.info("Can't check for updates: " + exception.getMessage());
	      }
	  });
  }
	
  public void onDisable() {
	  if (getConfig().getBoolean("shutdown-on-disable")) {
		  server.shutdown();
	  }
  }
}
