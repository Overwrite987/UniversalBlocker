package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class SyntaxBlocker implements Listener {

	private final Main main = Main.getInstance();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSyntax(PlayerCommandPreprocessEvent e) {
	    FileConfiguration config = main.getConfig();
	    FileConfiguration messageConfig = Config.messages;
	    String message = e.getMessage().toLowerCase();
	    Player p = e.getPlayer();
	    if (startWithExcluded(message)) {
	        return;
	    }
	    for (String symbol : Config.blockedsymbol) {
	        if (message.contains(symbol) && !isAdmin(p)) {
	            String symbolMessage = (RGBcolors.translate(messageConfig.getString("messages.blockedsymbol"))).replace("%symbol%", symbol);
	            p.sendMessage(symbolMessage);
	            e.setCancelled(true);
	            if (config.getBoolean("settings.enable-sounds")) {
	                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
	                       (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
	            }
	            if (config.getBoolean("settings.enable-titles")) {
	                String[] titleMessages = messageConfig.getString("messages.blockedsymbol-title").split(":");
	                String title = RGBcolors.translate(titleMessages[0]);
	                String subtitle = (RGBcolors.translate(titleMessages[1])).replace("%symbol%", symbol);
	                int fadeIn = Integer.parseInt(titleMessages[2]);
	                int stay = Integer.parseInt(titleMessages[3]);
	                int fadeOut = Integer.parseInt(titleMessages[4]);
	                p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
	            }
	            if (config.getBoolean("settings.notify")) {
	                String notifyMessage = (RGBcolors.translate(messageConfig.getString("messages.notify-symbol").replace("%player%", p.getName()))).replace("%symbol%", symbol);
	                Bukkit.broadcast(notifyMessage, "ublocker.admin");
	                if (config.getBoolean("settings.enable-sounds")) {
	                	for (Player admin : Bukkit.getOnlinePlayers()) {
	                		if (admin.hasPermission("ublocker.admin")) {
	                            admin.playSound(admin.getLocation(), Sound.valueOf(config.getString("sounds.admin-notify.sound")),
	                                       (float)config.getDouble("sounds.admin-notify.volume"), (float)config.getDouble("sounds.admin-notify.pitch"));
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