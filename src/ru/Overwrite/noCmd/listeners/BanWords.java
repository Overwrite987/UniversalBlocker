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
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class BanWords implements Listener {
	
	private final Main main = Main.getInstance();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		String message = e.getMessage().toLowerCase();
		Player p = e.getPlayer();
		for (String banword : Config.banwords) {
			if (message.contains(banword.toLowerCase()) && !isAdmin(p)) {
				cancelChatEvent(p, banword, e);
			}
		}
	} 
	      
	private void cancelChatEvent(Player p, String banword, Cancellable e) {
		e.setCancelled(true);
		FileConfiguration config = main.getConfig();
		FileConfiguration messageconfig = Config.messages;
		p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedword")).replace("%word%", banword));
		if (config.getBoolean("settings.enable-sounds")) {
			p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-chat.sound")),
				   (float)config.getDouble("sounds.blocked-chat.volume"), (float)config.getDouble("sounds.blocked-chat.pitch"));
		}
		if (config.getBoolean("settings.enable-titles")) {
	    	String[] titleMessages = messageconfig.getString("messages.blockedword-title").split(":");
			String title = RGBcolors.translate(titleMessages[0]);
			String subtitle = RGBcolors.translate(titleMessages[1]).replace("%symbol%", banword);
			int fadeIn = Integer.parseInt(titleMessages[2]);
			int stay = Integer.parseInt(titleMessages[3]);
			int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
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
	
	private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.banwords") || Config.excludedplayers.contains(player.getName());
    }
}