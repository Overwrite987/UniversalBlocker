package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class BanWords implements Listener {
	
	public static boolean active = false;
	
	Main main;	
	public BanWords(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        Config.setupBanWords();
        active = true;
        this.main = main;
        main.getLogger().info("> words-blocker - enabled");
    }
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
	  FileConfiguration config = main.getConfig();
	  FileConfiguration messageconfig = Config.messages;
	  String message = e.getMessage().toLowerCase();
	  Player p = e.getPlayer();
	  for (String banword : Config.banwords) {
	    if (message.contains(banword.toLowerCase()) && !isAdmin(p)) {
	      p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedword")).replace("%word%", banword));
	      e.setCancelled(true);
	      if (config.getBoolean("settings.enable-sounds")) {
              p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
                      (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
          }
	      if (config.getBoolean("settings.enable-titles")) {
	           p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blockedword-title").split(":")[0]), 
	        		   RGBcolors.translate(messageconfig.getString("messages.blockedword-title").split(":")[1]).replace("%word%", banword));
	      }
	      if (config.getBoolean("settings.notify")) {
		    Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-blockedword").replace("%player%", p.getName()).replace("%word%", banword)), "ublocker.admin");
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
	
	private boolean isAdmin(Player p) {
	  if (p.hasPermission("ublocker.bypass.banwords") || Config.excludedplayers.contains(p.getName())) {
		  return true;
	  }
	  return false;
	}
}