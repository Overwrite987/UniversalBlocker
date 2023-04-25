package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class BlockSyntax implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public BlockSyntax(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent e) {
	 	Player p = e.getPlayer();
	 	if (e.getMessage().split(" ")[0].contains(":") && !pluginConfig.excludedplayers.contains(p.getName())) {
	 		e.setCancelled(true);
	 		p.sendMessage(pluginConfig.messages_blocksyntax);
	   	  	if (pluginConfig.settings_enable_sounds) {
	   	  		p.playSound(p.getLocation(), pluginConfig.sounds_blocked_command_sound,
	   	  			   pluginConfig.sounds_blocked_command_volume, pluginConfig.sounds_blocked_command_pitch);
	   	  	}
	   	  	if (pluginConfig.settings_enable_titles) {
	   	  		plugin.sendTitleMessage(pluginConfig.titles_blocksyntax.split(":"), p);
	   	  	}
	   	  	if (pluginConfig.settings_notify) {
	   	  		String notifyMessage = pluginConfig.notify_blocksyntax.replace("%player%", p.getName()).replace("%cmd%", e.getMessage());
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
	}
}
