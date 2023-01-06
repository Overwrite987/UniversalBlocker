package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.configuration.file.FileConfiguration;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class CommandBlocker implements Listener {
	
	Main main;	
	public CommandBlocker(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        this.main = main;
        main.getLogger().info("command-blocker - enabled");
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	  public void onBlockedCommand(PlayerCommandPreprocessEvent e) {
		FileConfiguration config = Main.getInstance().getConfig();
		FileConfiguration messageconfig = Config.messages;
		String com = e.getMessage();
	    Player p = e.getPlayer();
	    for (String command : config.getStringList("blocked-commands.lite")) {
	      if ((com.toLowerCase().startsWith("/" + command + " ") || com.equalsIgnoreCase("/" + command)) && !isAdmin(p)) {
	    	p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedcommand")).replace("%cmd%", command));
	        e.setCancelled(true);
	        if (config.getBoolean("settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
                        (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
            }
	        if (config.getBoolean("settings.enable-titles")) {
	            p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blockedcommand-title").split(":")[0]), 
	         		   RGBcolors.translate(messageconfig.getString("messages.blockedcommand-title").split(":")[1]));
	        }
	        if (config.getBoolean("settings.notify")) {
	       	  Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-cmd").replace("%player%", p.getName()).replace("%cmd%", command)), "ublocker.admin");
	       	  for (Player ps : Bukkit.getOnlinePlayers()) {
				if (ps.hasPermission("ublocker.admin")) {
				    ps.playSound(ps.getLocation(), Sound.valueOf(config.getString("sounds.admin-notify.sound")),
				             (float)config.getDouble("sounds.admin-notify.volume"), (float)config.getDouble("sounds.admin-notify.pitch")); 
				}
		      }
	        }
	        continue;
	      } 
	    }
	    for (String command : config.getStringList("blocked-commands.full")) {
		  if ((com.toLowerCase().startsWith("/" + command + " ") || com.equalsIgnoreCase("/" + command)) && !config.getStringList("excluded-players").contains(p.getName())) {
		   	p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedcommand")).replace("%cmd%", command));
		    e.setCancelled(true);
		    if (config.getBoolean("settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
                        (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
            }
		    if (config.getBoolean("settings.enable-titles")) {
	            p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blockedcommand-title").split(":")[0]), 
	         		   RGBcolors.translate(messageconfig.getString("messages.blockedcommand-title").split(":")[1]));
	        }
		    if (config.getBoolean("settings.notify")) {
		     Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-cmd").replace("%player%", p.getName()).replace("%cmd%", command)), "ublocker.admin");
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
	
	private boolean isAdmin(Player p) {
		FileConfiguration config = Main.getInstance().getConfig();
	  if (p.hasPermission("ublocker.bypass.commands") || config.getStringList("excluded-players").contains(p.getName())) {
		  return true;
	  }
	  return false;
	}
}
