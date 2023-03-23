package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.Overwrite.noCmd.utils.Config;

public class CommandBlocker implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockedCommand(PlayerCommandPreprocessEvent e) {
	    String com = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    for (String command : Config.liteblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!isAdmin(p)) {
	                p.sendMessage(Config.messages_blockedcommand.replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(p, command);
	            }
	            return;
	        }
	    }
	    for (String command : Config.fullblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!Config.excludedplayers.contains(p.getName())) {
	                p.sendMessage(Config.messages_blockedcommand.replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(p, command);
	            }
	            return;
	        }
	    }
	}

	private void handleBlockedCommand(Player p, String command) {
	    if (Config.settings_enable_sounds) {
	        p.playSound(p.getLocation(), Config.sounds_blocked_command_sound,
	                Config.sounds_blocked_command_volume, Config.sounds_blocked_command_pitch);
	    }
	    if (Config.settings_enable_titles) {
	    	String[] titleMessages = Config.titles_blockedcommand.split(":");
 	    	String title = titleMessages[0];
			String subtitle = titleMessages[1].replace("%cmd%", command);
			int fadeIn = Integer.parseInt(titleMessages[2]);
			int stay = Integer.parseInt(titleMessages[3]);
			int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	    }
	    if (Config.settings_notify) {
	    	String notifyMessage = Config.notify_blockedcommand.replace("%player%", p.getName()).replace("%cmd%", command);
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
        return player.hasPermission("ublocker.bypass.commands") || Config.excludedplayers.contains(player.getName());
    }
}
