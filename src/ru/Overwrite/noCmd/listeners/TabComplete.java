package ru.Overwrite.noCmd.listeners;

import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class TabComplete implements Listener {
	
	public static boolean active = false;
	
	Main main;	
	public TabComplete(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        Config.setupArgshidden();
        active = true;
        this.main = main;
        main.getLogger().info("> args-hider - enabled");
    }
	
	@EventHandler
	  public void onTabComplete(AsyncTabCompleteEvent e) {
		if (!(e.getSender() instanceof Player)) {
		  return;
		}
		Player p = (Player)e.getSender();
		for (String command : Config.argshidedcmds) {
		  if (e.getBuffer().equalsIgnoreCase("/" + command + " ") && !isAdmin(p)) {
			e.setCancelled(true);
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
