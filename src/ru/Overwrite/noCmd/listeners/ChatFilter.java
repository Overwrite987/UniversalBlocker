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

public class ChatFilter implements Listener {
	
	Main main;	
	public ChatFilter(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        this.main = main;
        main.getLogger().info("allowed-chars - enabled");
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatMessage(AsyncPlayerChatEvent e) {
      FileConfiguration config = Main.getInstance().getConfig();
      FileConfiguration messageconfig = Config.messages;
	  String message = e.getMessage();
	  Player p = e.getPlayer();
	  if (message != null && containsBlockedChars(message) && !isAdmin(p)) {
	      p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedchatsymbol")));
	      e.setCancelled(true);
	      if (config.getBoolean("settings.enable-sounds")) {
              p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
                      (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
          }
	      if (config.getBoolean("settings.enable-titles")) {
	           p.sendTitle(RGBcolors.translate(messageconfig.getString("messages.blockedchatsymbol-title").split(":")[0]), 
	        		   RGBcolors.translate(messageconfig.getString("messages.blockedchatsymbol-title").split(":")[1]));
	      }
	    if (config.getBoolean("settings.notify")) {
	      Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-chatsymbol").replace("%player%", p.getName()).replace("%chatsymbol%", message)), "ublocker.admin");
	      for (Player ps : Bukkit.getOnlinePlayers()) {
			if (ps.hasPermission("ublocker.admin")) {
			    ps.playSound(ps.getLocation(), Sound.valueOf(config.getString("sounds.admin-notify.sound")),
			             (float)config.getDouble("sounds.admin-notify.volume"), (float)config.getDouble("sounds.admin-notify.pitch")); 
			}
	      }
	    }
	  } 
	}
	
	private boolean isAdmin(Player p) {
		FileConfiguration config = Main.getInstance().getConfig();
	  if (p.hasPermission("ublocker.bypass.chatsymbol") || config.getStringList("excluded-players").contains(p.getName())) {
		  return true;
	  }
	  return false;
	}
	  
    private boolean containsBlockedChars(String message) {
	  FileConfiguration config = Main.getInstance().getConfig();
	    char[] d = message.toLowerCase().toCharArray();
	    int b = d.length;
        for (int f = 0; f < b; f++) {
	    char c = d[f];
	  if (config.getString("chat-settings.allowed-chars").indexOf(c) == -1)
		 return true; 
	   } 
	 return false;
   }
}
