package ru.Overwrite.noCmd.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.noCmd.Main;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class Config {
	
	private static final Main instance = Main.getInstance();
	
	public static FileConfiguration messages;
	
	public static Set<String> fullblocked, liteblocked, banwords, blockedsymbol, blockedsignsymbol, excludedcommands, excludedplayers, argshidedcmds, consoleblocked, rconblocked;
	public static String allowedchars;

    public static void loadMessages() {
        File file = new File(instance.getDataFolder(), "message.yml");
        if (instance.getResource("message.yml") == null) {
            save(YamlConfiguration.loadConfiguration(file), "message.yml");
        }
        if (!file.exists()) {
        	instance.saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        if (instance.debug) {
        	instance.getLogger().info("> messages.yml загружен");
        }
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(instance.getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
    public static void setupExcluded(FileConfiguration config) {
        excludedplayers = new HashSet<>(config.getStringList("excluded-players"));
        if (instance.debug) {
            instance.getLogger().info("§e> Невосприимчивые игроки загружены");
        }
    }
    
    public static void setupCommands(FileConfiguration config) {
    	fullblocked = new HashSet<>(config.getStringList("blocked-commands.full"));
    	liteblocked = new HashSet<>(config.getStringList("blocked-commands.lite"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Списки блокировок команд загружены");
    	}
    }
    
    public static void setupBanWords(FileConfiguration config) {
    	banwords = new HashSet<>(config.getStringList("chat-settings.ban-words"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Списки заблокированных слов загружены");
    	}
    }
    
    public static void setupChars(FileConfiguration config) {
    	allowedchars = config.getString("chat-settings.allowed-chars");
    	if (instance.debug) {
    		instance.getLogger().info("§e> Список разрешенных символов чата загружен");
    	}
    }
    
    public static void setupSyntax(FileConfiguration config) {
    	blockedsymbol = new HashSet<>(config.getStringList("symbols.blocked-symbols"));
    	excludedcommands = new HashSet<>(config.getStringList("symbols.excluded-commands"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Списки блокировок символов команд загружены");
    	}
    }
    
    public static void setupSignSyntax(FileConfiguration config) {
    	blockedsignsymbol = new HashSet<>(config.getStringList("symbols.blocked-sign-symbols"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Списки блокировок символов табличек загружены");
    	}
    }
    
    public static void setupArgshidden(FileConfiguration config) {
    	argshidedcmds = new HashSet<>(config.getStringList("blocked-commands.args-tab-complete"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Команды с заблокированными аргументами загружены");
    	}
    }
    
    public static void setupRcon(FileConfiguration config) {
    	rconblocked = new HashSet<>(config.getStringList("blocked-commands.rcon"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Заблокированные команды rcon загружены");
    	}
    }
    
    public static void setupConsole(FileConfiguration config) {
    	consoleblocked = new HashSet<>(config.getStringList("blocked-commands.console"));
    	if (instance.debug) {
    		instance.getLogger().info("§e> Заблокированные команды консоли загружены");
    	}
    }
}
