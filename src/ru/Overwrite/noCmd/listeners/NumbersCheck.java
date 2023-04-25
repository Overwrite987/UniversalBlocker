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
	
	final Main plugin;
	private final Config pluginConfig;
	
	public NumbersCheck(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	private final Pattern IP_PATTERN = Pattern.compile("(\\d+\\.){3}");

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatNumber(AsyncPlayerChatEvent e) {
	    FileConfiguration config = plugin.getConfig();
	    String message = e.getMessage();
	    Player p = e.getPlayer();
	    int limit = config.getInt("chat-settings.maxmsg-numbers");
	    if (pluginConfig.chat_settings_strict_number_chek) {
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
		 FileConfiguration config = plugin.getConfig();
		 p.sendMessage(pluginConfig.messages_maxnumbers.replace("%limit%", config.getString("chat-settings.maxmsg-numbers")));
		 if (pluginConfig.settings_enable_sounds) {
             p.playSound(p.getLocation(), pluginConfig.sounds_blocked_chat_sound,
                    pluginConfig.sounds_blocked_chat_volume, pluginConfig.sounds_blocked_chat_pitch);
         }
 	     if (pluginConfig.settings_enable_titles) {
 	    	 plugin.sendTitleMessage(pluginConfig.titles_maxnumbers.split(":"), p);
 	     }
 	     if (pluginConfig.settings_notify) {
 	    	String notifyMessage = pluginConfig.notify_maxnumbers.replace("%player%", p.getName()).replace("%limit%", config.getString("chat-settings.maxmsg-numbers")).replace("%msg%", message);
 	    	for (Player admin : Bukkit.getOnlinePlayers()) {
 	    		if (admin.hasPermission("ublocker.admin")) {
 	    			admin.sendMessage(notifyMessage);
 	    			if (pluginConfig.settings_enable_sounds) {
 	        			admin.playSound(admin.getLocation(), pluginConfig.sounds_admin_notify_sound,
 	        					pluginConfig.sounds_admin_notify_volume, pluginConfig.sounds_admin_notify_pitch); 
 	        		 }
 	        	 }
 	         }
 	     }	
	}
	
	private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.numbers") || pluginConfig.excludedplayers.contains(player.getName());
    }

}

