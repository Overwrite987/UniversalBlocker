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

public class BlockSyntax implements Listener {
	 
	Main main;	
	public BlockSyntax(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        this.main = main;
        main.getLogger().info("blocksyntax - enabled");
    }
	
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCommand(PlayerCommandPreprocessEvent e) {
	  FileConfiguration config = Main.getInstance().getConfig();
	  FileConfiguration messageconfig = Config.messages;
	  Player p = e.getPlayer();
	 if (e.getMessage().split(" ")[0].contains(":") && !config.getStringList("excluded-players").contains(p.getName())) {
	   e.setCancelled(true);
	   p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blocksyntax")));
	   if (config.getBoolean("settings.enable-sounds")) {
           p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
                   (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
       }
	   if (config.getBoolean("settings.enable-titles")) {
           p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blocksyntax-title").split(":")[0]), 
        		   RGBcolors.translate(messageconfig.getString("messages.blocksyntax-title").split(":")[1]));
       }
	  if (config.getBoolean("settings.notify")) {
	    Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-blocksyntax").replace("%player%", p.getName()).replace("%cmd%", e.getMessage())), "ublocker.admin");
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
