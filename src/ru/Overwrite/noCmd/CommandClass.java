package ru.Overwrite.noCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.Overwrite.noCmd.utils.Config;

public class CommandClass implements CommandExecutor {
  private final Main plugin;
  
  public CommandClass(Main plugin) {
    this.plugin = plugin;
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
    cmd.getName().equalsIgnoreCase("ub");
    if (!sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§7This server is using §cUniversalBlocker §7by §5Overwrite");
      return true;
    } 
    if (args.length == 0 && sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§6/ub reload - перезагрузить конфиг");
      return true;
    } 
    if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("ublocker.admin")) {
      this.plugin.reloadConfig();
      Config.loadMessages();
      sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена");
      return true;
    } 
    if (!args[0].equalsIgnoreCase("reload") && args.length > 1) {
      sender.sendMessage("§6/ub reload - перезагрузить конфиг");
    } else {
      sender.sendMessage("§7This server is using §cUniversalBlocker §7by §5Overwrite");
    } 
    return true;
  }
}
