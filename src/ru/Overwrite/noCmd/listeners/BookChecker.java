package ru.Overwrite.noCmd.listeners;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class BookChecker implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public BookChecker(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBookEdit(PlayerEditBookEvent e) {
		List<String> messages = e.getNewBookMeta().getPages();
		Player p = e.getPlayer();
		for (String message : messages) {
			if (message != null && containsBlockedChars(message) && !isAdmin(p)) {
				cancelBookEvent(p, message, e);
			}
		}
	}
	
	private void cancelBookEvent(Player p, String message, Cancellable e) {
		e.setCancelled(true);
	    p.sendMessage(pluginConfig.messages_blockedbooksymbol);
	    if (pluginConfig.settings_enable_sounds) {
            p.playSound(p.getLocation(), pluginConfig.sounds_blocked_book_sound,
                    pluginConfig.sounds_blocked_book_volume, pluginConfig.sounds_blocked_book_pitch);
        }
	    if (pluginConfig.settings_enable_titles) {
	    	plugin.sendTitleMessage(pluginConfig.titles_blockedbooksymbol.split(":"), p);
	    }
	    if (pluginConfig.settings_notify) {
	    	String notifyMessage = pluginConfig.notify_blockedbooksymbol.replace("%player%", p.getName());
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
        return player.hasPermission("ublocker.bypass.booksymbols") || pluginConfig.excludedplayers.contains(player.getName());
    }
	  
	private boolean containsBlockedChars(String message) {
	    char[] characters = message.toLowerCase().toCharArray();
	    int length = characters.length;
	    for (int i = 0; i < length; i++) {
	        char character = characters[i];
	        if (pluginConfig.allowedbookchars.indexOf(character) == -1) {
	            return true; 
	        } 
	    }
	    return false;
	}
}
