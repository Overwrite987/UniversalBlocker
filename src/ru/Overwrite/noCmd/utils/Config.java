package ru.Overwrite.noCmd.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.noCmd.Main;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class Config {
	
	public static FileConfiguration messages;
	
	public static Set<String> fullblocked;
	public static Set<String> liteblocked;
	public static Set<String> banwords;
	public static Set<String> blockedsymbol;
	public static Set<String> excludedcommands;
	public static Set<String> excludedplayers;
	public static Set<String> argshidedcmds;
	public static String allowedchars;

    public static void loadMessages() {
        File file = new File(Main.getInstance().getDataFolder(), "message.yml");
        if (Main.getInstance().getResource("message.yml") == null) {
            save(YamlConfiguration.loadConfiguration(file), "message.yml");
        }
        if (!file.exists()) {
        	Main.getInstance().saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        Main.getInstance().getLogger().info("> messages.yml загружен");
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(Main.getInstance().getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
    public static void setupExcluded() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	excludedplayers = new HashSet<String>(config.getStringList("excluded-players"));
    	Main.getInstance().getLogger().info("§e> Невосприимчивые игроки загружены");
    }
    
    public static void setupCommands() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	fullblocked = new HashSet<String>(config.getStringList("blocked-commands.full"));
    	liteblocked = new HashSet<String>(config.getStringList("blocked-commands.lite"));
    	Main.getInstance().getLogger().info("§e> Списки блокировок команд загружены");
    }
    
    public static void setupBanWords() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	banwords = new HashSet<String>(config.getStringList("chat-settings.ban-words"));
    	Main.getInstance().getLogger().info("§e> Списки заблокированных слов загружены");
    }
    
    public static void setupChars() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	allowedchars = config.getString("chat-settings.allowed-chars");
    	Main.getInstance().getLogger().info("§e> Список разрешенных символов чата загружен");
    }
    
    public static void setupSyntax() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	blockedsymbol = new HashSet<String>(config.getStringList("symbols.blocked-symbols"));
    	excludedcommands = new HashSet<String>(config.getStringList("symbols.excluded-commands"));
    	Main.getInstance().getLogger().info("§e> Списки блокировок символов команд загружены");
    }
    
    public static void setupArgshidden() {
    	FileConfiguration config = Main.getInstance().getConfig();
    	argshidedcmds = new HashSet<String>(config.getStringList("blocked-commands.args-tab-complete"));
    	Main.getInstance().getLogger().info("§e> Команды с заблокированными аргументами загружены");
    }
    
}
