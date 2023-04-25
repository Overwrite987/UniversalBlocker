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

public class BanWords implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public BanWords(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		String message = e.getMessage().toLowerCase();
		Player p = e.getPlayer();
		for (String banword : pluginConfig.banwords) {
			if (message.contains(banword.toLowerCase()) && !isAdmin(p)) {
				cancelChatEvent(p, banword, e);
			}
		}
	} 
	      
	private void cancelChatEvent(Player p, String banword, Cancellable e) {
		e.setCancelled(true);
		p.sendMessage(pluginConfig.messages_blockedword.replace("%word%", banword));
		if (pluginConfig.settings_enable_sounds) {
			p.playSound(p.getLocation(), pluginConfig.sounds_blocked_chat_sound,
				   pluginConfig.sounds_blocked_chat_volume, pluginConfig.sounds_blocked_chat_pitch);
		}
		if (pluginConfig.settings_enable_titles) {
			plugin.sendTitleMessage(pluginConfig.titles_blockedword.replace("%word%", banword).split(":"), p);
		}
		if (pluginConfig.settings_notify) {
			String notifyMessage = pluginConfig.notify_blockedword.replace("%player%", p.getName()).replace("%word%", banword);
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
        return player.hasPermission("ublocker.bypass.banwords") || pluginConfig.excludedplayers.contains(player.getName());
    }
}