package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.Overwrite.noCmd.utils.Config;

public class SyntaxBlocker implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSyntax(PlayerCommandPreprocessEvent e) {
	    String message = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    if (startWithExcluded(message)) {
	        return;
	    }
	    for (String symbol : Config.blockedsymbol) {
	        if (message.contains(symbol) && !isAdmin(p)) {
	            p.sendMessage(Config.messages_blockedsymbol.replace("%symbol%", symbol));
	            e.setCancelled(true);
	            if (Config.settings_enable_sounds) {
	                p.playSound(p.getLocation(), Config.sounds_blocked_command_sound,
	                       Config.sounds_blocked_command_volume, Config.sounds_blocked_command_pitch);
	            }
	            if (Config.settings_enable_titles) {
	                String[] titleMessages = Config.titles_blockedsymbol.split(":");
	                String title = titleMessages[0];
	                String subtitle = titleMessages[1].replace("%symbol%", symbol);
	                int fadeIn = Integer.parseInt(titleMessages[2]);
	                int stay = Integer.parseInt(titleMessages[3]);
	                int fadeOut = Integer.parseInt(titleMessages[4]);
	                p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	            }
	            if (Config.settings_notify) {
	            	String notifyMessage = Config.notify_blockedsymbol.replace("%player%", p.getName()).replace("%symbol%", symbol);
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

	private boolean startWithExcluded(String com) {
	    for (String excluded : Config.excludedcommands) {
	        if (com.toLowerCase().startsWith("/" + excluded + " ")) {
	           return true;
	        }
	    }
	    return false;
	}

	private boolean isAdmin(Player p) {
	    return p.hasPermission("ublocker.bypass.symbol") || Config.excludedplayers.contains(p.getName());
	}
}