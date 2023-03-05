package ru.Overwrite.noCmd;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.Overwrite.noCmd.utils.Config;

public class CommandClass implements CommandExecutor {
	
  private final Main main = Main.getInstance();
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
    if (!sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§7This server is using §cUniversalBlocker §7by §5Overwrite");
      return true;
    } 
    if (args.length == 0 && sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
      return true;
    } 
    if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("ublocker.admin")) {
      main.reloadConfig();
      FileConfiguration config = main.getConfig();
      Config.loadMessages();
      Config.setupExcluded(config);
      if (!Config.banwords.isEmpty()) {
    	  Config.setupBanWords(config);
      }
      if (!Config.blockedsymbol.isEmpty()) {
    	  Config.setupSyntax(config);
      }
      if (!Config.blockedsymbol.isEmpty()) {
    	  Config.setupSignSyntax(config);
      }
      if (!Config.allowedchars.isEmpty()) {
    	  Config.setupChars(config);
      }
      if (!Config.liteblocked.isEmpty() && !Config.fullblocked.isEmpty()) {
    	  Config.setupCommands(config);
      }
      if (!Config.argshidedcmds.isEmpty()) {
    	  Config.setupArgshidden(config);
      }
      if (!Config.consoleblocked.isEmpty()) {
    	  Config.setupConsole(config);
      }
      if (!Config.rconblocked.isEmpty()) {
    	  Config.setupRcon(config);
      }
      sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена");
      return true;
    } 
    if (!args[0].equalsIgnoreCase("reload") && args.length > 1) {
      sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
      if (main.debug) {
    	  Logger logger = main.getLogger();
    	  logger.info("§6Фулл-блокед командс §7:" + Config.fullblocked.toString()); 
    	  logger.info("§6Лайт-блокед командс §7:" + Config.liteblocked.toString());
    	  logger.info("§6Банворды §7:" + Config.banwords.toString());
    	  logger.info("§6Блокедсимволс §7:" + Config.blockedsymbol.toString());
    	  logger.info("§6Блокедсигнсимволс §7:" + Config.blockedsignsymbol.toString()); 
    	  logger.info("§6Эксплюдедкомандс §7:" + Config.excludedcommands.toString()); 
    	  logger.info("§6Эксклюдедплеерс §7:" + Config.excludedplayers.toString());
    	  logger.info("§6Аргсхайден §7:" + Config.argshidedcmds.toString());
    	  logger.info("§6Консольблокед §7:" + Config.consoleblocked.toString());
    	  logger.info("§6Рконблокед §7:" + Config.rconblocked.toString());
      }
    } else {
      sender.sendMessage("§7This server is using §cUniversalBlocker §7by §5Overwrite");
    } 
    return true;
  }
}
