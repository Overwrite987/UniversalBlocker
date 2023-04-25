package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class SyntaxBlocker implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public SyntaxBlocker(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSyntax(PlayerCommandPreprocessEvent e) {
	    String message = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    if (startWithExcluded(message)) {
	        return;
	    }
	    for (String symbol : pluginConfig.blockedsymbol) {
	        if (message.contains(symbol) && !isAdmin(p)) {
	            p.sendMessage(pluginConfig.messages_blockedsymbol.replace("%symbol%", symbol));
	            e.setCancelled(true);
	            if (pluginConfig.settings_enable_sounds) {
	                p.playSound(p.getLocation(), pluginConfig.sounds_blocked_command_sound,
	                		pluginConfig.sounds_blocked_command_volume, pluginConfig.sounds_blocked_command_pitch);
	            }
	            if (pluginConfig.settings_enable_titles) {
	            	plugin.sendTitleMessage(pluginConfig.titles_blockedsymbol.replace("%symbol%", symbol).split(":"), p);
	            }
	            if (pluginConfig.settings_notify) {
	            	String notifyMessage = pluginConfig.notify_blockedsymbol.replace("%player%", p.getName()).replace("%symbol%", symbol);
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

	private boolean startWithExcluded(String com) {
	    for (String excluded : pluginConfig.excludedcommands) {
	        if (com.toLowerCase().startsWith("/" + excluded + " ")) {
	           return true;
	        }
	    }
	    return false;
	}

	private boolean isAdmin(Player p) {
	    return p.hasPermission("ublocker.bypass.symbol") || pluginConfig.excludedplayers.contains(p.getName());
	}
}