package ru.Overwrite.noCmd.listeners;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.RGBcolors;
import ru.Overwrite.noCmd.utils.Config;

public class NumbersCheck implements Listener {
	
	private final Main main = Main.getInstance();
	
	private final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatNumber(AsyncPlayerChatEvent e) {
	    FileConfiguration config = main.getConfig();
	    FileConfiguration messageconfig = Config.messages;
	    String message = e.getMessage();
	    Player p = e.getPlayer();
	    int limit = config.getInt("chat-settings.maxmsg-numbers");
	    if (config.getBoolean("chat-settings.strict-number-chek")) {
		    int count = 0;
		    for (int a = 0, b = message.length(); a < b; a++) {
		      char c = message.charAt(a);
		      if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || 
		          c == '6' || c == '7' || c == '8' || c == '9')
		          count++; 
		      } 
		      if (count > limit && !isAdmin(p)) {
		    	  cancelChatEvent(p, message, e);
		      } 
	    } else {
	        Matcher matcher = IP_PATTERN.matcher(message);
	        int digitsCount = 0;

	        while (matcher.find()) {
	            String[] parts = matcher.group().split("\\.");
	            for (String part : parts) {
	                digitsCount += part.length();
	            }
	        }

	        if (digitsCount > limit && !isAdmin(p)) {
	            cancelChatEvent(p, message, e);
	        }
	    }
	}
	
	private void cancelChatEvent(Player p, String message, Cancellable e) {
		e.setCancelled(true);
		FileConfiguration config = main.getConfig();
		FileConfiguration messageconfig = Config.messages;
		p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.maxnumbers-msg").replace("%limit%", config.getString("chat-settings.maxmsg-numbers"))));
		if (config.getBoolean("settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
                    (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
        }
 	    if (config.getBoolean("settings.enable-titles")) {
 	    	String[] titleMessages = messageconfig.getString("messages.maxnumbers-title").split(":");
 	    	String title = RGBcolors.translate(titleMessages[0]);
			String subtitle = RGBcolors.translate(titleMessages[1]);
			int fadeIn = Integer.parseInt(titleMessages[2]);
			int stay = Integer.parseInt(titleMessages[3]);
			int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
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
	
	private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.numbers") || Config.excludedplayers.contains(player.getName());
    }

}

