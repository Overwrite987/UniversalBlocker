package ru.Overwrite.noCmd.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class ConsoleBlocker implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public ConsoleBlocker(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsoleCommand(ServerCommandEvent e) {
        String cmd = e.getCommand();
        for (String command : pluginConfig.consoleblocked) {
        	if (cmd.startsWith(command + " ") || cmd.equals(command)) {
        		e.setCancelled(true); 
        	}
        }
    }
}
