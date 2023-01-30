package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.RGBcolors;
import ru.Overwrite.noCmd.utils.Config;

public class NumbersCheck implements Listener {
	
	Main main;	
	public NumbersCheck(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        this.main = main;
        main.getLogger().info("> numbers-check - enabled");
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	  public void onChatNumber(AsyncPlayerChatEvent e) {
	  FileConfiguration config = main.getConfig();
	  FileConfiguration messageconfig = Config.messages;
	    String message = e.getMessage();
	    Player p = e.getPlayer();
	    int count = 0;
	    int limit = config.getInt("chat-settings.maxmsg-numbers");
	    for (int a = 0, b = message.length(); a < b; a++) {
	      char c = message.charAt(a);
	      if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || 
	        c == '6' || c == '7' || c == '8' || c == '9')
	        count++; 
	    } 
	    if (count > limit && !isAdmin(p)) {
	      p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.maxnumbers-msg").replace("%limit%", config.getString("chat-settings.maxmsg-numbers"))));
	      e.setCancelled(true);
	      if (config.getBoolean("settings.enable-sounds")) {
              p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
                      (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
          }
	      if (config.getBoolean("settings.enable-titles")) {
	            p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.maxnumbers-title").split(":")[0]), 
	         		   RGBcolors.translate(messageconfig.getString("messages.maxnumbers-title").split(":")[1]));
	      }
	      if (config.getBoolean("settings.notify")) {
	          Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-maxnumbers")
	        		  .replace("%player%", p.getName()).replace("%limit%", config.getString("chat-settings.maxmsg-numbers")).replace("%msg%", message)), "ublocker.admin");
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
	
	private boolean isAdmin(Player p) {
	  if (p.hasPermission("ublocker.bypass.numbers") || Config.excludedplayers.contains(p.getName())) {
		  return true;
	  }
	  return false;
	}

}

