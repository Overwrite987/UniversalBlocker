package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.utils.Config;

public class ChatFilter implements Listener {
	
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
	    p.sendMessage(Config.messages_blockedchatsymbol);
	    if (Config.settings_enable_sounds) {
            p.playSound(p.getLocation(), Config.sounds_blocked_chat_sound,
                    Config.sounds_blocked_chat_volume, Config.sounds_blocked_chat_pitch);
        }
	    if (Config.settings_enable_titles) {
	    	String[] titleMessages = Config.titles_blockedchatsymbol.split(":");
	    	String title = titleMessages[0];
	    	String subtitle = titleMessages[1];
	    	int fadeIn = Integer.parseInt(titleMessages[2]);
	    	int stay = Integer.parseInt(titleMessages[3]);
	    	int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	    }
	    if (Config.settings_notify) {
	    	String notifyMessage = Config.notify_blockedchatsymbol.replace("%player%", p.getName()).replace("%chatsymbol%", message);
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
        return player.hasPermission("ublocker.bypass.chatsymbols") || Config.excludedplayers.contains(player.getName());
    }
	  
	private boolean containsBlockedChars(String message) {
	    char[] characters = message.toLowerCase().toCharArray();
	    int length = characters.length;
	    for (int i = 0; i < length; i++) {
	        char character = characters[i];
	        if (Config.allowedchars.indexOf(character) == -1) {
	            return true; 
	        } 
	    }
	    return false;
	}
}
