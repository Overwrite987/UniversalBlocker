package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.Listener;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class SignSymbolBlocker implements Listener {
	
public static boolean active = false;
	
	Main main;	
	public SignSymbolBlocker(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        Config.setupSignSyntax();
        active = true;
        this.main = main;
        if (main.debug) {
        	main.getLogger().info("> sign-symbol-blocker - enabled");
        }
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSyntax(SignChangeEvent e) {
	    FileConfiguration config = main.getConfig();
	    FileConfiguration messageconfig = Config.messages;
	    String line0 = e.getLine(0);
		String line1 = e.getLine(1);
		String line2 = e.getLine(2);
		String line3 = e.getLine(3);
	    Player player = e.getPlayer();
	    for (String symbol : Config.blockedsignsymbol) {
	        if ((line0.contains(symbol) || line1.contains(symbol) || line2.contains(symbol) || line3.contains(symbol)) && !isAdmin(player)) {
	            String symbolMessage = RGBcolors.translate(messageconfig.getString("messages.blockedsignsymbol")).replace("%symbol%", symbol);
	            player.sendMessage(symbolMessage);
	            e.setCancelled(true);

	            if (config.getBoolean("settings.enable-sounds")) {
	                player.playSound(player.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
	                        (float)config.getDouble("sounds.blocked-command.volume"), (float)config.getDouble("sounds.blocked-command.pitch"));
	            }

	            if (config.getBoolean("settings.enable-titles")) {
	                String[] titleMessages = messageconfig.getString("messages.blockedsignsymbol-title").split(":");
	                String title = RGBcolors.translate(titleMessages[0]);
	                String subtitle = RGBcolors.translate(titleMessages[1]).replace("%symbol%", symbol);
	                player.sendTitle(title, subtitle);
	            }

	            if (config.getBoolean("settings.notify")) {
	                String notifyMessage = RGBcolors.translate(messageconfig.getString("messages.notify-signsymbol").replace("%player%", player.getName()).replace("%symbol%", symbol));
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
	
	private boolean isAdmin(Player p) {
		  if (p.hasPermission("ublocker.bypass.symbol") || Config.excludedplayers.contains(p.getName())) {
			  return true;
		  }
		  return false;
		}
}
