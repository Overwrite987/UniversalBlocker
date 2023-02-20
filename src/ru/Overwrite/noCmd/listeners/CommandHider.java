package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class CommandHider implements Listener {
	
	public static boolean active = false;
	
	Main main;	
	public CommandHider(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        if (!CommandBlocker.active) {
            Config.setupCommands();
        }
        active = true;
        this.main = main;
        if (main.debug) {
        	main.getLogger().info("> command-hider - enabled");
        }
    }
	
	@EventHandler
	  public void onCommandSend(PlayerCommandSendEvent e) {
	    FileConfiguration config = main.getConfig();
	    Player p = e.getPlayer();
	    if (!isAdmin(p)) {
	      e.getCommands().removeIf(cmd -> Config.liteblocked.contains(cmd) || Config.fullblocked.contains(cmd));
	      if (config.getBoolean("settings.enable-blocksyntax")) {
	        e.getCommands().removeIf(cmd -> cmd.contains(":"));
	      }
	    } 
	  }
	
	private boolean isAdmin(Player p) {
	  if (p.hasPermission("ublocker.bypass.tabcomplete") || Config.excludedplayers.contains(p.getName())) {
		  return true;
	  }
	  return false;
	}
}