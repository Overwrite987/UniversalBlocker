package ru.Overwrite.noCmd.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandSendEvent;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class CommandHider implements Listener {
	
	private final Main main = Main.getInstance();
	
	@EventHandler
	  public void onCommandSend(PlayerCommandSendEvent e) {
	    FileConfiguration config = main.getConfig();
	    Player p = e.getPlayer();
	    if (!isAdmin(p)) {
	      e.getCommands().removeIf(cmd -> Config.liteblocked.contains(cmd) || Config.fullblocked.contains(cmd));
	      if (config.getBoolean("settings.enable-blocksyntax")) {
	        e.getCommands().removeIf(cmd -> cmd.contains(":"));
	      }
	    } 
	  }
	
	private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.tabcomplete") || Config.excludedplayers.contains(player.getName());
    }
}