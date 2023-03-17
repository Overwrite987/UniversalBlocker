package ru.Overwrite.noCmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.Consumer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.noCmd.listeners.*;
import ru.Overwrite.noCmd.utils.*;

public class Main extends JavaPlugin {
	
  public boolean debug;
  
  private final PluginManager pluginManager = getServer().getPluginManager();
	
  private static Main instance;
	  
  public static Main getInstance() {
	  return instance;
  }
 
  public final int SUB_VERSION = Integer.parseInt(
       getServer().getClass().getPackage().getName()
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
    Config.loadMessages();
    Config.setupExcluded(config);
    if (config.getBoolean("settings.debug")) {
    	debug = true;
    }
    if (config.getBoolean("settings.enable-metrics")) {
      new Metrics(this, 15379);
    }
    if (config.getBoolean("settings.update-checker")) {
    	checkUpdates(this, version -> {
            getLogger().info("§6========================================");
            if (getDescription().getVersion().equals(version)) {
                getLogger().info("§aВы используете последнюю версию плагина!");
            } else {
                getLogger().info("§aВы используете устаревшую или некорректную версию плагина!");
                getLogger().info("§aВы можете загрузить последнюю версию плагина здесь:");
                getLogger().info("§bhttps://github.com/Overwrite987/UniversalBlocker/releases/");
            }
            getLogger().info("§6========================================");
        });
    }
    if (config.getBoolean("settings.enable-blocksyntax")) {
    	pluginManager.registerEvents(new BlockSyntax(), this);
    	if (debug) {
    		getLogger().info("§6> BlockSyntax - enabled");
        }
    }
    if (config.getBoolean("settings.enable-words-blocker")) {
    	Config.setupBanWords(config);
    	pluginManager.registerEvents(new BanWords(), this);
    	if (debug) {
        	getLogger().info("§6> BanWords - enabled");
        }
    }
    if (config.getBoolean("settings.enable-allowed-chars")) {
    	Config.setupChars(config);
    	pluginManager.registerEvents(new ChatFilter(), this);
    	if (debug) {
        	getLogger().info("§6> ChatFilter - enabled");
        }
    }
    if (config.getBoolean("settings.enable-command-blocker")) {
        Config.setupCommands(config);
        pluginManager.registerEvents(new CommandBlocker(), this);
        if (debug) {
        	getLogger().info("§6> CommandBlocker - enabled");
        }
    }
    if (SUB_VERSION >= 13 && getConfig().getBoolean("settings.hide-blocked-commands-from-tab-comple")) {
    	if (Config.liteblocked.isEmpty() && Config.fullblocked.isEmpty()) {
        	Config.setupCommands(config);
        }
    	pluginManager.registerEvents(new CommandHider(), this);
        if (debug) {
        	getLogger().info("§6> CommandHider - enabled");
        }
    } else {
    	getLogger().info("Скрытие из таб-комплита не доступно на вашей версии!");
    }
    if (config.getBoolean("settings.enable-console-blocker")) {
    	pluginManager.registerEvents(new ConsoleBlocker(), this);
    	Config.setupConsole(config);
    	if (debug) {
        	getLogger().info("§6> ConsoleBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-numbers-check")) {
    	pluginManager.registerEvents(new NumbersCheck(), this);
    	if (debug) {
        	getLogger().info("§6> NumbersCheck - enabled");
        }
    }
    if (config.getBoolean("settings.enable-rcon-blocker")) {
    	pluginManager.registerEvents(new RconBlocker(), this);
    	Config.setupRcon(config);
    	if (debug) {
        	getLogger().info("§6> RconBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-symbol-blocker")) {
    	Config.setupSyntax(config);
    	pluginManager.registerEvents(new SyntaxBlocker(), this);
    	if (debug) {
        	getLogger().info("§6> SyntaxBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-sign-symbol-blocker")) {
    	Config.setupSignSyntax(config);
    	pluginManager.registerEvents(new SignSymbolBlocker(), this);
    	if (debug) {
        	getLogger().info("§6> SignSymbolBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-tab-complere-blocker")) {
    	Config.setupArgshidden(config);
    	pluginManager.registerEvents(new TabComplete(), this);
    	if (debug) {
        	getLogger().info("§6> TabComplete - enabled");
        }
    }
    long endTime = System.currentTimeMillis();
    getLogger().info("Plugin started in " + (endTime - startTime) + " ms");
  }
  
  private void checkPaper() {
  	if (getServer().getName().equals("CraftBukkit")) {
          getLogger().info("§6============= §6! WARNING ! §c=============");
          getLogger().info("§eЭтот плагин работает только на Paper и его форках!");
          getLogger().info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
          getLogger().info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
          getLogger().info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
          getLogger().info("§6============= §6! WARNING ! §c=============");
          setEnabled(false);
          return;
      }    	
  }
  
  private void checkUpdates(Plugin plugin, Consumer<String> consumer) {
	  getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
	      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION").openStream()))) {
	    	  String version = reader.readLine();
	          if (version != null) {
	              consumer.accept(version.trim());
	          }
	      } catch (IOException exception) {
	          plugin.getLogger().info("Can't check for updates: " + exception.getMessage());
	      }
	  });
  }
	
  public void onDisable() {
	  if (getConfig().getBoolean("shutdown-on-disable")) {
		  getServer().shutdown();
	  }
  }
}
