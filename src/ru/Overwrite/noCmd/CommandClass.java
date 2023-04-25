package ru.Overwrite.noCmd;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.Overwrite.noCmd.utils.Config;

public class CommandClass implements CommandExecutor {
	
  final Main plugin;
  private final Config pluginConfig;
	
  public CommandClass(Main plugin) {
      this.plugin = plugin;
      pluginConfig = plugin.getPluginConfig();
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
    if (!sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker v1.11§7 by §5OverwriteMC");
      return true;
    } 
    if (args.length == 0 && sender.hasPermission("ublocker.admin")) {
      sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
      return true;
    } 
    if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("ublocker.admin")) {
      plugin.reloadConfig();
      FileConfiguration config = plugin.getConfig();
      plugin.debug = config.getBoolean("settings.debug");
      pluginConfig.loadBooleans(config);
      pluginConfig.loadMessageFile();
      pluginConfig.loadMessages();
      pluginConfig.loadNotifies();
      pluginConfig.loadTitles();
      pluginConfig.setupExcluded(config);
      if (config.getBoolean("settings.enable-sounds")) {
    	  pluginConfig.setupSounds(config);
      }
      if (!pluginConfig.banwords.isEmpty()) {
    	  pluginConfig.setupBanWords(config);
      }
      if (!pluginConfig.blockedsymbol.isEmpty()) {
    	  pluginConfig.setupSyntax(config);
      }
      if (!pluginConfig.blockedsymbol.isEmpty()) {
    	  pluginConfig.setupSignSyntax(config);
      }
      if (!pluginConfig.allowedchars.isEmpty()) {
    	  pluginConfig.setupChars(config);
      }
      if (!pluginConfig.liteblocked.isEmpty() && !pluginConfig.fullblocked.isEmpty()) {
    	  pluginConfig.setupCommands(config);
      }
      if (!pluginConfig.argshidedcmds.isEmpty()) {
    	  pluginConfig.setupArgshidden(config);
      }
      if (!pluginConfig.consoleblocked.isEmpty()) {
    	  pluginConfig.setupConsole(config);
      }
      if (!pluginConfig.rconblocked.isEmpty()) {
    	  pluginConfig.setupRcon(config);
      }
      sender.sendMessage("§cUniversalBlocker §7> §aКонфигурация перезагружена");
      return true;
    } 
    if (!args[0].equalsIgnoreCase("reload") && args.length > 1) {
      sender.sendMessage("§6/" + commandlabel + " reload - перезагрузить конфиг");
      if (plugin.debug) {
    	  Logger logger = plugin.logger;
    	  logger.info("§6Нотифай§7: " + pluginConfig.settings_notify);
    	  logger.info("§6Енабле титлы§7: " + pluginConfig.settings_enable_titles);
    	  logger.info("§6Енабле саундсы§7: " + pluginConfig.settings_enable_sounds);
    	  logger.info("§6Стрикт намбер чек§7: " + pluginConfig.chat_settings_strict_number_chek);
    	  logger.info("§6Фулл-блокед командс§7: " + pluginConfig.fullblocked.toString()); 
    	  logger.info("§6Лайт-блокед командс§7: " + pluginConfig.liteblocked.toString());
    	  logger.info("§6Банворды§7: " + pluginConfig.banwords.toString());
    	  logger.info("§6Блокедсимволс§7: " + pluginConfig.blockedsymbol.toString());
    	  logger.info("§6Блокедсигнсимволс§7: " + pluginConfig.blockedsignsymbol.toString()); 
    	  logger.info("§6Эксплюдедкомандс§7: " + pluginConfig.excludedcommands.toString()); 
    	  logger.info("§6Эксклюдедплеерс§7: " + pluginConfig.excludedplayers.toString());
    	  logger.info("§6Аргсхайден§7: " + pluginConfig.argshidedcmds.toString());
    	  logger.info("§6Консольблокед§7: " + pluginConfig.consoleblocked.toString());
    	  logger.info("§6Рконблокед§7: " + pluginConfig.rconblocked.toString());
      }
    } else {
      sender.sendMessage("§6❖ §7Running §c§lUniversalBlocker v1.11§7 by §5OverwriteMC");
    } 
    return true;
  }
}
