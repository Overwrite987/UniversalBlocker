package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class ChatFilter implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public ChatFilter(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatMessage(AsyncPlayerChatEvent e) {
		String message = e.getMessage();
		Player p = e.getPlayer();
		if (message != null && containsBlockedChars(message) && !isAdmin(p)) {
			cancelChatEvent(p, message, e);
		}
	}
	
	private void cancelChatEvent(Player p, String message, Cancellable e) {
		e.setCancelled(true);
	    p.sendMessage(pluginConfig.messages_blockedchatsymbol);
	    if (pluginConfig.settings_enable_sounds) {
            p.playSound(p.getLocation(), pluginConfig.sounds_blocked_chat_sound,
                    pluginConfig.sounds_blocked_chat_volume, pluginConfig.sounds_blocked_chat_pitch);
        }
	    if (pluginConfig.settings_enable_titles) {
	    	plugin.sendTitleMessage(pluginConfig.titles_blockedchatsymbol.split(":"), p);
	    }
	    if (pluginConfig.settings_notify) {
	    	String notifyMessage = pluginConfig.notify_blockedchatsymbol.replace("%player%", p.getName()).replace("%chatsymbol%", message);
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
        return player.hasPermission("ublocker.bypass.chatsymbols") || pluginConfig.excludedplayers.contains(player.getName());
    }
	  
	private boolean containsBlockedChars(String message) {
	    char[] characters = message.toLowerCase().toCharArray();
	    int length = characters.length;
	    for (int i = 0; i < length; i++) {
	        char character = characters[i];
	        if (pluginConfig.allowedchars.indexOf(character) == -1) {
	            return true; 
	        } 
	    }
	    return false;
	}
}
