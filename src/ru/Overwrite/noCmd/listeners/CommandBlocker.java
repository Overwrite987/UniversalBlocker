package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class CommandBlocker implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public CommandBlocker(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockedCommand(PlayerCommandPreprocessEvent e) {
	    String com = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    for (String command : pluginConfig.liteblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!isAdmin(p)) {
	                p.sendMessage(pluginConfig.messages_blockedcommand.replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(p, command);
	            }
	            return;
	        }
	    }
	    for (String command : pluginConfig.fullblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!pluginConfig.excludedplayers.contains(p.getName())) {
	                p.sendMessage(pluginConfig.messages_blockedcommand.replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(p, command);
	            }
	            return;
	        }
	    }
	}

	private void handleBlockedCommand(Player p, String command) {
	    if (pluginConfig.settings_enable_sounds) {
	        p.playSound(p.getLocation(), pluginConfig.sounds_blocked_command_sound,
	                pluginConfig.sounds_blocked_command_volume, pluginConfig.sounds_blocked_command_pitch);
	    }
	    if (pluginConfig.settings_enable_titles) {
	    	plugin.sendTitleMessage(pluginConfig.titles_blockedcommand.replace("%cmd%", command).split(":"), p);
	    }
	    if (pluginConfig.settings_notify) {
	    	String notifyMessage = pluginConfig.notify_blockedcommand.replace("%player%", p.getName()).replace("%cmd%", command);
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
        return player.hasPermission("ublocker.bypass.commands") || pluginConfig.excludedplayers.contains(player.getName());
    }
}
