package ru.Overwrite.noCmd.listeners;

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

public class ChatFilter implements Listener {
	
	private final Main main = Main.getInstance();
	
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
		FileConfiguration config = main.getConfig();
	    FileConfiguration messageconfig = Config.messages;
	    p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedchatsymbol")));
	    if (config.getBoolean("settings.enable-sounds")) {
            p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
                    (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
        }
	    if (config.getBoolean("settings.enable-titles")) {
	    	String[] titleMessages = messageconfig.getString("messages.blockedchatsymbol-title").split(":");
	    	String title = RGBcolors.translate(titleMessages[0]);
	    	String subtitle = RGBcolors.translate(titleMessages[1]);
	    	int fadeIn = Integer.parseInt(titleMessages[2]);
	    	int stay = Integer.parseInt(titleMessages[3]);
	    	int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	    }
	    if (config.getBoolean("settings.notify")) {
	    	Bukkit.broadcast(RGBcolors.translate(messageconfig.getString("messages.notify-chatsymbol").replace("%player%", p.getName()).replace("%chatsymbol%", message)), "ublocker.admin");
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
