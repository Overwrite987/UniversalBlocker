package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.Overwrite.noCmd.utils.Config;

public class BanWords implements Listener {
	
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
		p.sendMessage(Config.messages_blockedword.replace("%word%", banword));
		if (Config.settings_enable_sounds) {
			p.playSound(p.getLocation(), Config.sounds_blocked_chat_sound,
				   Config.sounds_blocked_chat_volume, Config.sounds_blocked_chat_pitch);
		}
		if (Config.settings_enable_titles) {
	    	String[] titleMessages = Config.titles_blockedword.split(":");
			String title = titleMessages[0];
			String subtitle = titleMessages[1].replace("%symbol%", banword);
			int fadeIn = Integer.parseInt(titleMessages[2]);
			int stay = Integer.parseInt(titleMessages[3]);
			int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		}
		if (Config.settings_notify) {
			String notifyMessage = Config.notify_blockedword.replace("%player%", p.getName()).replace("%word%", banword);
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
        return player.hasPermission("ublocker.bypass.banwords") || Config.excludedplayers.contains(player.getName());
    }
}