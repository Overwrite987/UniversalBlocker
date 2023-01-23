package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class SyntaxBlocker implements Listener {
	
	public static boolean active = false;
	
	Main main;	
	public SyntaxBlocker(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        Config.setupSyntax();
        active = true;
        this.main = main;
        main.getLogger().info("> symbol-blocker - enabled");
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSyntax(PlayerCommandPreprocessEvent e) {
      FileConfiguration config = main.getConfig();
      FileConfiguration messageconfig = Config.messages;
	  for (String symbol : Config.blockedsymbol) {
	    String com = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    if (!startWithExcluded(com)) {
	      if (com.contains(symbol.toLowerCase()) && !isAdmin(p)) {
	    	p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedsymbol")).replace("%symbol%", symbol));
	        e.setCancelled(true);
	        if (config.getBoolean("settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
                        (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
            }
	        if (config.getBoolean("settings.enable-titles")) {
	            p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blockedsymbol-title").split(":")[0]), 
	         		   RGBcolors.translate(messageconfig.getString("messages.blockedsymbol-title").split(":")[1]).replace("%symbol%", symbol));
	        }
	        if (config.getBoolean("settings.notify")) {
	          Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-symbol").replace("%player%", p.getName()).replace("%symbol%", symbol)), "ublocker.admin");
	          if (config.getBoolean("settings.enable-sounds")) {
	        	for (Player ps : Bukkit.getOnlinePlayers()) {
	        	  if (ps.hasPermission("ublocker.admin")) {
	        		  ps.playSound(ps.getLocation(), Sound.valueOf(config.getString("sounds.admin-notify.sound")),
	                          (float)config.getDouble("sounds.admin-notify.volume"), (float)config.getDouble("sounds.admin-notify.pitch")); 
	        	  }
	        	}
	          }
	        }
	      }
	    }
      }
	}
	
	private boolean startWithExcluded(String com) {
	   for (String excluded : Config.excludedcommands) {
	     if (com.toLowerCase().startsWith("/" + excluded + " ")) {
	    	 return true;
	     }
	  }
	  return false;  
	}
	
	private boolean isAdmin(Player p) {
	  if (p.hasPermission("ublocker.bypass.symbol") || Config.excludedplayers.contains(p.getName())) {
		  return true;
	  }
	  return false;
	}
}
