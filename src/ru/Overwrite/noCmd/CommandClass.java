package ru.Overwrite.noCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ru.Overwrite.noCmd.listeners.BanWords;
import ru.Overwrite.noCmd.listeners.SyntaxBlocker;
import ru.Overwrite.noCmd.listeners.ChatFilter;
import ru.Overwrite.noCmd.listeners.CommandBlocker;
import ru.Overwrite.noCmd.listeners.CommandHider;
import ru.Overwrite.noCmd.listeners.TabComplete;
import ru.Overwrite.noCmd.utils.Config;

public class CommandClass implements CommandExecutor {
  
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
      Main.getInstance().reloadConfig();
      Config.loadMessages();
      Config.setupExcluded();
      if (BanWords.active) {
    	  Config.setupBanWords();
      }
      if (SyntaxBlocker.active) {
    	  Config.setupSyntax();
      }
      if (ChatFilter.active) {
    	  Config.setupChars();
      }
      if (CommandBlocker.active) {
    	  Config.setupCommands();
      }
      if (CommandHider.active && !CommandBlocker.active) {
    	  Config.setupCommands();
      }
      if (TabComplete.active) {
    	  Config.setupArgshidden();
      }
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
