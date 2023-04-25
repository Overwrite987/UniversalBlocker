package ru.Overwrite.noCmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.noCmd.listeners.*;
import ru.Overwrite.noCmd.utils.*;

public class Main extends JavaPlugin {
	
  private final Config pluginConfig = new Config(this);
  
  public boolean debug;
  
  public final Logger logger = getLogger();
 
  public static final int SUB_VERSION = Integer.parseInt(
		  Bukkit.getServer().getClass().getPackage().getName()
          		.replace(".", ",")
          		.split(",")[3]
          		.replace("1_", "")
          		.replaceAll("_R\\d", "")
          		.replace("v", "")
  );
  
  public void onEnable() {
	long startTime = System.currentTimeMillis();
	checkPaper();
    saveDefaultConfig();
    FileConfiguration config = getConfig();
    if (config.getBoolean("settings.debug")) {
    	debug = true;
    }
    pluginConfig.loadBooleans(config);
    pluginConfig.loadMessageFile();
    pluginConfig.loadMessages();
    pluginConfig.loadNotifies();
    pluginConfig.loadTitles();
    pluginConfig.setupExcluded(config);
    if (config.getBoolean("settings.enable-sounds")) {
    	pluginConfig.setupSounds(config);
    }
    if (config.getBoolean("settings.enable-metrics")) {
      new Metrics(this, 15379);
    }
    if (config.getBoolean("settings.update-checker")) {
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
    PluginManager pluginManager = Bukkit.getPluginManager();
    if (config.getBoolean("settings.enable-blocksyntax")) {
    	pluginManager.registerEvents(new BlockSyntax(this), this);
    	if (debug) {
    		logger.info("§6> BlockSyntax - enabled");
        }
    }
    if (config.getBoolean("settings.enable-words-blocker")) {
    	pluginConfig.setupBanWords(config);
    	pluginManager.registerEvents(new BanWords(this), this);
    	if (debug) {
        	logger.info("§6> BanWords - enabled");
        }
    }
    if (config.getBoolean("settings.enable-allowed-chars")) {
    	pluginConfig.setupChars(config);
    	pluginManager.registerEvents(new ChatFilter(this), this);
    	if (debug) {
        	logger.info("§6> ChatFilter - enabled");
        }
    }
    if (config.getBoolean("settings.enable-command-blocker")) {
        pluginConfig.setupCommands(config);
        pluginManager.registerEvents(new CommandBlocker(this), this);
        if (debug) {
        	logger.info("§6> CommandBlocker - enabled");
        }
    }
    if (SUB_VERSION >= 13 && getConfig().getBoolean("settings.hide-blocked-commands-from-tab-comple")) {
    	if (pluginConfig.liteblocked.isEmpty() && pluginConfig.fullblocked.isEmpty()) {
        	pluginConfig.setupCommands(config);
        }
    	pluginManager.registerEvents(new CommandHider(this), this);
        if (debug) {
        	logger.info("§6> CommandHider - enabled");
        }
    } else {
    	logger.info("Скрытие из таб-комплита не доступно на вашей версии!");
    }
    if (config.getBoolean("settings.enable-console-blocker")) {
    	pluginManager.registerEvents(new ConsoleBlocker(this), this);
    	pluginConfig.setupConsole(config);
    	if (debug) {
        	logger.info("§6> ConsoleBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-numbers-check")) {
    	pluginManager.registerEvents(new NumbersCheck(this), this);
    	if (debug) {
        	logger.info("§6> NumbersCheck - enabled");
        }
    }
    if (config.getBoolean("settings.enable-rcon-blocker")) {
    	pluginManager.registerEvents(new RconBlocker(this), this);
    	pluginConfig.setupRcon(config);
    	if (debug) {
        	logger.info("§6> RconBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-symbol-blocker")) {
    	pluginConfig.setupSyntax(config);
    	pluginManager.registerEvents(new SyntaxBlocker(this), this);
    	if (debug) {
        	logger.info("§6> SyntaxBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-sign-symbol-blocker")) {
    	pluginConfig.setupSignSyntax(config);
    	pluginManager.registerEvents(new SignSymbolBlocker(this), this);
    	if (debug) {
        	logger.info("§6> SignSymbolBlocker - enabled");
        }
    }
    if (config.getBoolean("settings.enable-tab-complere-blocker")) {
    	pluginConfig.setupArgshidden(config);
    	pluginManager.registerEvents(new TabComplete(this), this);
    	if (debug) {
        	logger.info("§6> TabComplete - enabled");
        }
    }
    getCommand("universalblocker").setExecutor(new CommandClass(this));
    long endTime = System.currentTimeMillis();
    logger.info("Plugin started in " + (endTime - startTime) + " ms");
  }
  
  private void checkPaper() {
	  if (Bukkit.getName().equals("CraftBukkit")) {
          logger.info("§6============= §6! WARNING ! §c=============");
          logger.info("§eЭтот плагин работает только на Paper и его форках!");
          logger.info("§eАвтор плагина §cкатегорически §eвыступает за отказ от использования устаревшего и уязвимого софта!");
          logger.info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
          logger.info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
          logger.info("§6============= §6! WARNING ! §c=============");
          setEnabled(false);
      }
  }
  
  public Config getPluginConfig() {
	  return pluginConfig;
  }
  
  public void sendTitleMessage(String[] titleMessages, Player p) {
		String title = titleMessages[0];
		String subtitle = titleMessages[1];
		int fadeIn = Integer.parseInt(titleMessages[2]);
		int stay = Integer.parseInt(titleMessages[3]);
		int fadeOut = Integer.parseInt(titleMessages[4]);
		p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
  }
  
  private void checkUpdates(Plugin plugin, Consumer<String> consumer) {
	  if (!Bukkit.getName().equals("Folia")) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION").openStream()))) {
					String version = reader.readLine();
					if (version != null) {
						consumer.accept(version.trim());
					}
				} catch (IOException exception) {
					logger.info("Can't check for updates: " + exception.getMessage());
				}
			});
		} else {
			Bukkit.getGlobalRegionScheduler().run(plugin, (u) -> {
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
  }
	
  public void onDisable() {
	  if (getConfig().getBoolean("shutdown-on-disable")) {
		  Bukkit.shutdown();
	  }
  }
}
