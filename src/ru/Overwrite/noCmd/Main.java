package ru.Overwrite.noCmd;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.noCmd.listeners.*;
import ru.Overwrite.noCmd.utils.*;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class Main extends JavaPlugin {
	
  public boolean debug = false;
	
  private static Main instance;
	  
  public static Main getInstance() {
     return instance;
  }
 
  public final int SUB_VERSION = Integer.parseInt(
       Bukkit.getServer().getClass().getPackage().getName()
               .replace(".", ",")
               .split(",")[3]
               .replace("1_", "")
               .replaceAll("_R\\d", "")
               .replace("v", "")
  );
  
  public void onEnable() {
	  if (getServer().getName().equals("CraftBukkit")) {
		  getLogger().info("§6=============§6! WARNING ! §c=============");
		  getLogger().info("§eЭтот плагин работает только на Paper и его форках!");
		  getLogger().info("§eСкачать Paper для новых версий: §ahttps://papermc.io/downloads");
		  getLogger().info("§eСкачать Paper для старых версий: §ahttps://papermc.io/legacy §7((в тесте выбирайте 2 вариант ответа))");
		  getLogger().info("§6=============§6! WARNING ! §c=============");
		  setEnabled(false);
		  return;
	  }
	instance = this;
    getCommand("universalblocker").setExecutor(new CommandClass());
    saveDefaultConfig();
    Config.loadMessages();
    Config.setupExcluded();
    if (getConfig().getBoolean("debug")) {
    	debug = true;
    }
    if (getConfig().getBoolean("settings.enable-metrics")) {
      new Metrics(this, 15379);
    }
    if (getConfig().getBoolean("settings.update-checker")) {
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
    if (getConfig().getBoolean("settings.enable-blocksyntax")) {
        new BlockSyntax(this);
    }
    if (getConfig().getBoolean("settings.enable-words-blocker")) {
    	new BanWords(this);
    }
    if (getConfig().getBoolean("settings.enable-allowed-chars")) {
    	new ChatFilter(this);
    }
    if (getConfig().getBoolean("settings.enable-command-blocker")) {
        new CommandBlocker(this);
    }
    if (SUB_VERSION >= 13 && getConfig().getBoolean("settings.hide-blocked-commands-from-tab-comple")) {
        new CommandHider(this);
    } else {
    	getLogger().info("Скрытие из таб-комплита не доступно на вашей версии!");
    }
    if (getConfig().getBoolean("settings.enable-console-blocker")) {
        new ConsoleBlocker(this);
    }
    if (getConfig().getBoolean("settings.enable-numbers-check")) {
        new NumbersCheck(this);
    }
    if (getConfig().getBoolean("settings.enable-rcon-blocker")) {
        new RconBlocker(this);
    }
    if (getConfig().getBoolean("settings.enable-symbol-blocker")) {
        new SyntaxBlocker(this);
    }
    if (getConfig().getBoolean("settings.enable-sign-symbol-blocker")) {
        new SignSymbolBlocker(this);
    }
    if (getConfig().getBoolean("settings.enable-tab-complere-blocker")) {
        new TabComplete(this);
    }
  }
  
  private static void checkUpdates(Plugin plugin, Consumer<String> consumer) {
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
          try (Scanner scanner = new Scanner(new URL("https://raw.githubusercontent.com/Overwrite987/UniversalBlocker/master/VERSION").openStream())) {
              if (scanner.hasNext()) {
                  consumer.accept(scanner.next());
              }
          } catch (IOException exception) {
              plugin.getLogger().info("Can't check for updates: " + exception.getMessage());
          }
      });
  }
	
  public void onDisable() {
    if (getConfig().getBoolean("shutdown-on-disable")) {
	  Bukkit.shutdown();
    }
  }
}
