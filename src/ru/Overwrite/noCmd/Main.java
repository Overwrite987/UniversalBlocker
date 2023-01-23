package ru.Overwrite.noCmd;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.Overwrite.noCmd.listeners.*;
import ru.Overwrite.noCmd.utils.*;

public class Main extends JavaPlugin {
	
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
    getCommand("universalblocker").setExecutor(new CommandClass(this));
    saveDefaultConfig();
    Config.loadMessages();
    Config.setupExcluded();
    if (getConfig().getBoolean("settings.enable-metrics")) {
      new Metrics(this, 15379);
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
    	getLogger().info("Функция скрытия команд из таб-комплита не доступна на вашей версии!");
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
    if (getConfig().getBoolean("settings.enable-tab-complere-blocker")) {
        new TabComplete(this);
    }
  }
	
  public void onDisable() {
    if (getConfig().getBoolean("shutdown-on-disable")) {
	  Bukkit.shutdown();
    }
  }
}
