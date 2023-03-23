package ru.Overwrite.noCmd.listeners;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class NumbersCheck implements Listener {
	
	private final Main main = Main.getInstance();
	
	private final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatNumber(AsyncPlayerChatEvent e) {
	    FileConfiguration config = main.getConfig();
	    String message = e.getMessage();
	    Player p = e.getPlayer();
	    int limit = config.getInt("chat-settings.maxmsg-numbers");
	    if (Config.chat_settings_strict_number_chek) {
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
		 p.sendMessage(Config.messages_maxnumbers.replace("%limit%", config.getString("chat-settings.maxmsg-numbers")));
		 if (Config.settings_enable_sounds) {
             p.playSound(p.getLocation(), Config.sounds_blocked_chat_sound,
                    Config.sounds_blocked_chat_volume, Config.sounds_blocked_chat_pitch);
         }
 	     if (Config.settings_enable_titles) {
 	    	 String[] titleMessages = Config.titles_maxnumbers.split(":");
 	    	 String title = titleMessages[0];
			 String subtitle = titleMessages[1];
			 int fadeIn = Integer.parseInt(titleMessages[2]);
			 int stay = Integer.parseInt(titleMessages[3]);
			 int fadeOut = Integer.parseInt(titleMessages[4]);
			 p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
 	     }
 	     if (Config.settings_notify) {
 	    	String notifyMessage = Config.notify_maxnumbers.replace("%player%", p.getName()).replace("%limit%", config.getString("chat-settings.maxmsg-numbers")).replace("%msg%", message);
 	    	for (Player admin : Bukkit.getOnlinePlayers()) {
 	    		if (admin.hasPermission("ublocker.admin")) {
 	    			admin.sendMessage(notifyMessage);
 	    			if (Config.settings_enable_sounds) {
 	        			admin.playSound(admin.getLocation(), Config.sounds_admin_notify_sound,
 	        					Config.sounds_admin_notify_volume, Config.sounds_admin_notify_pitch); 
 	        		 }
 	        	 }
 	         }
 	     }	
	}
	
	private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.numbers") || Config.excludedplayers.contains(player.getName());
    }

}

