package ru.Overwrite.noCmd.listeners;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class TabComplete implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public TabComplete(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}

    @EventHandler
    public void onTabComplete(AsyncTabCompleteEvent e) {
        if (!(e.getSender() instanceof Player)) {
            return;
        }
        Player p = (Player) e.getSender();
        String buffer = e.getBuffer();
        if ((buffer.split(" ").length == 1 && !buffer.endsWith(" ")) || !buffer.startsWith("/")) {
            e.setCancelled(true);
            return;
        }
        for (String command : pluginConfig.argshidedcmds) {
            if (e.getBuffer().equalsIgnoreCase("/" + command + " ") && !isAdmin(p)) {
                e.setCancelled(true);
            }
        }
    }

    private boolean isAdmin(Player p) {
        return p.hasPermission("ublocker.bypass.tabcomplete") || pluginConfig.excludedplayers.contains(p.getName());
    }
}