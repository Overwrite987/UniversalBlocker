package ru.Overwrite.noCmd.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;
import ru.Overwrite.noCmd.utils.Config;

public class RconBlocker implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRconCommand(RemoteServerCommandEvent e) {
        String cmd = e.getCommand();
        for (String command : Config.rconblocked) {
        	if (cmd.startsWith(command + " ") || cmd.equals(command)) {
        		e.setCancelled(true); 
        	}
        }
    }
}