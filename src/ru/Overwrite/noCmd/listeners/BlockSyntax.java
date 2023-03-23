package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.Overwrite.noCmd.utils.Config;

public class BlockSyntax implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent e) {
	 	Player p = e.getPlayer();
	 	if (e.getMessage().split(" ")[0].contains(":") && !Config.excludedplayers.contains(p.getName())) {
	 		e.setCancelled(true);
	 		p.sendMessage(Config.messages_blocksyntax);
	   	  	if (Config.settings_enable_sounds) {
	   	  		p.playSound(p.getLocation(), Config.sounds_blocked_command_sound,
	   	  			   Config.sounds_blocked_command_volume, Config.sounds_blocked_command_pitch);
	   	  	}
	   	  	if (Config.settings_enable_titles) {
	   	  		String[] titleMessages = Config.titles_blocksyntax.split(":");
	   	  		String title = titleMessages[0];
	   	  		String subtitle = titleMessages[1];
	   	  		int fadeIn = Integer.parseInt(titleMessages[2]);
	   	  		int stay = Integer.parseInt(titleMessages[3]);
	   	  		int fadeOut = Integer.parseInt(titleMessages[4]);
	   	  		p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	   	  	}
	   	  	if (Config.settings_notify) {
	   	  		String notifyMessage = Config.notify_blocksyntax.replace("%player%", p.getName()).replace("%cmd%", e.getMessage());
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
	}
}
