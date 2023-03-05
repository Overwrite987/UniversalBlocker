package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.configuration.file.FileConfiguration;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class CommandBlocker implements Listener {
	
	private final Main main = Main.getInstance();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockedCommand(PlayerCommandPreprocessEvent e) {
	    String com = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    FileConfiguration config = main.getConfig();
	    FileConfiguration messageconfig = Config.messages;
	    for (String command : Config.liteblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!isAdmin(p)) {
	                p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedcommand")).replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(config, messageconfig, p, command);
	            }
	            return;
	        }
	    }
	    for (String command : Config.fullblocked) {
	        if (com.startsWith("/" + command + " ") || com.equals("/" + command)) {
	            if (!config.getStringList("excluded-players").contains(p.getName())) {
	                p.sendMessage(RGBcolors.translate(messageconfig.getString("messages.blockedcommand")).replace("%cmd%", command));
	                e.setCancelled(true);
	                handleBlockedCommand(config, messageconfig, p, command);
	            }
	            return;
	        }
	    }
	}

	private void handleBlockedCommand(FileConfiguration config, FileConfiguration messageconfig, Player p, String command) {
	    if (config.getBoolean("settings.enable-sounds")) {
	        p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
	                (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
	    }
	    if (config.getBoolean("settings.enable-titles")) {
	    	String[] titleMessages = messageconfig.getString("messages.blockedcommand-title").split(":");
 	    	String title = RGBcolors.translate(titleMessages[0]);
			String subtitle = RGBcolors.translate(titleMessages[1]).replace("%cmd%", command);
			int fadeIn = Integer.parseInt(titleMessages[2]);
			int stay = Integer.parseInt(titleMessages[3]);
			int fadeOut = Integer.parseInt(titleMessages[4]);
			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	    }
	    if (config.getBoolean("settings.notify")) {
	        String notifyMessage = messageconfig.getString("messages.notify-cmd")
	                .replace("%player%", p.getName()).replace("%cmd%", command);
	        Bukkit.broadcast(RGBcolors.translate(notifyMessage), "ublocker.admin");
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
        return player.hasPermission("ublocker.bypass.commands") || Config.excludedplayers.contains(player.getName());
    }
}
