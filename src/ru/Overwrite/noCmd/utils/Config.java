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
	
	public static Set<String> fullblocked;
	public static Set<String> liteblocked;
	public static Set<String> banwords;
	public static Set<String> blockedsymbol;
	public static Set<String> blockedsignsymbol;
	public static Set<String> excludedcommands;
	public static Set<String> excludedplayers;
	public static Set<String> argshidedcmds;
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
        instance.getLogger().info("> messages.yml загружен");
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(instance.getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
    public static void setupExcluded() {
    	FileConfiguration config = instance.getConfig();
    	excludedplayers = new HashSet<String>(config.getStringList("excluded-players"));
    	instance.getLogger().info("§e> Невосприимчивые игроки загружены");
    }
    
    public static void setupCommands() {
    	FileConfiguration config = instance.getConfig();
    	fullblocked = new HashSet<String>(config.getStringList("blocked-commands.full"));
    	liteblocked = new HashSet<String>(config.getStringList("blocked-commands.lite"));
    	instance.getLogger().info("§e> Списки блокировок команд загружены");
    }
    
    public static void setupBanWords() {
    	FileConfiguration config = instance.getConfig();
    	banwords = new HashSet<String>(config.getStringList("chat-settings.ban-words"));
    	instance.getLogger().info("§e> Списки заблокированных слов загружены");
    }
    
    public static void setupChars() {
    	FileConfiguration config = instance.getConfig();
    	allowedchars = config.getString("chat-settings.allowed-chars");
    	instance.getLogger().info("§e> Список разрешенных символов чата загружен");
    }
    
    public static void setupSyntax() {
    	FileConfiguration config = instance.getConfig();
    	blockedsymbol = new HashSet<String>(config.getStringList("symbols.blocked-symbols"));
    	excludedcommands = new HashSet<String>(config.getStringList("symbols.excluded-commands"));
    	instance.getLogger().info("§e> Списки блокировок символов команд загружены");
    }
    
    public static void setupSignSyntax() {
    	FileConfiguration config = instance.getConfig();
    	blockedsignsymbol = new HashSet<String>(config.getStringList("symbols.blocked-sign-symbols"));
    	instance.getLogger().info("§e> Списки блокировок символов табличек загружены");
    }
    
    public static void setupArgshidden() {
    	FileConfiguration config = instance.getConfig();
    	argshidedcmds = new HashSet<String>(config.getStringList("blocked-commands.args-tab-complete"));
    	instance.getLogger().info("§e> Команды с заблокированными аргументами загружены");
    }
    
}
