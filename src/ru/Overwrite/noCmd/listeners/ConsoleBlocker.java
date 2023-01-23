package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import ru.Overwrite.noCmd.Main;

public class ConsoleBlocker implements Listener {
	
	Main main;	
	public ConsoleBlocker(Main main) {
        Bukkit.getPluginManager().registerEvents(this, main);
        this.main = main;
        main.getLogger().info("> console-blocker - enabled");
    }
 
  @EventHandler
  public void onConsoleCommand(ServerCommandEvent e) {
 	String cmd = e.getCommand().replace("/", "");
	   if (consoleBoolean(cmd))
 	     e.setCancelled(true); 
  }
 
  private boolean consoleBoolean(String message) {
	  FileConfiguration config = main.getConfig();
	  for (String s : config.getStringList("blocked-commands.console")) {
	    if (s.equalsIgnoreCase(message))
	      return true; 
	  } 
	  return false;
    }
}
