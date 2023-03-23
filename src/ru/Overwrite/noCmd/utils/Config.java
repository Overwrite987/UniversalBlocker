package ru.Overwrite.noCmd.utils;

import org.bukkit.Sound;
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
	
	public static Set<String> 
	fullblocked, 
	liteblocked, 
	banwords, 
	blockedsymbol, 
	blockedsignsymbol, 
	excludedcommands, 
	excludedplayers, 
	argshidedcmds, 
	consoleblocked, 
	rconblocked;
	
	public static String 
	allowedchars,
	messages_blockedcommand,
	messages_blockedsymbol,
	messages_blockedsignsymbol,
	messages_blockedchatsymbol,
	messages_blockedword,
	messages_blocksyntax,
	messages_maxnumbers,
	notify_blockedcommand,
	notify_blockedsymbol,
	notify_blockedsignsymbol,
	notify_blockedchatsymbol,
	notify_blockedword,
	notify_blocksyntax,
	notify_maxnumbers,
	titles_blockedcommand,
	titles_blockedsymbol,
	titles_blockedsignsymbol,
	titles_blockedchatsymbol,
	titles_blockedword,
	titles_blocksyntax,
	titles_maxnumbers;
	
	public static Sound
	sounds_admin_notify_sound,
	sounds_blocked_command_sound,
	sounds_blocked_symbol_sound,
	sounds_blocked_chat_sound;
	
	public static boolean
	settings_notify,
	settings_enable_titles,
	settings_enable_sounds,
	chat_settings_strict_number_chek;
	
	public static float
	sounds_admin_notify_volume,
	sounds_admin_notify_pitch,
	sounds_blocked_command_volume,
	sounds_blocked_command_pitch,
	sounds_blocked_symbol_volume,
	sounds_blocked_symbol_pitch,
	sounds_blocked_chat_volume,
	sounds_blocked_chat_pitch;

    public static void loadMessageFile() {
        File file = new File(instance.getDataFolder(), "message.yml");
        if (instance.getResource("message.yml") == null) {
            save(YamlConfiguration.loadConfiguration(file), "message.yml");
        }
        if (!file.exists()) {
        	instance.saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        if (instance.debug) {
        	instance.getLogger().info("§a> messages.yml загружен");
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
    
    public static void loadBooleans(FileConfiguration config) {
    	settings_notify = config.getBoolean("settings.notify");
    	settings_enable_titles = config.getBoolean("settings.enable-titles");
    	settings_enable_sounds = config.getBoolean("settings.enable-sounds");
    	chat_settings_strict_number_chek = config.getBoolean("chat-settings.strict-number-chek");
    	if (instance.debug) {
            instance.getLogger().info("§e> Настройки загружены");
        }
    }
    
    public static void loadMessages() {
    	messages_blockedcommand = RGBcolors.translate(messages.getString("messages.blockedcommand"));
    	messages_blockedsymbol = RGBcolors.translate(messages.getString("messages.blockedsymbol"));
    	messages_blockedsignsymbol = RGBcolors.translate(messages.getString("messages.blockedsignsymbol"));
    	messages_blockedchatsymbol = RGBcolors.translate(messages.getString("messages.blockedchatsymbol"));
    	messages_blockedword = RGBcolors.translate(messages.getString("messages.blockedword"));
    	messages_blocksyntax = RGBcolors.translate(messages.getString("messages.blocksyntax"));
    	messages_maxnumbers = RGBcolors.translate(messages.getString("messages.maxnumbers"));
    	if (instance.debug) {
            instance.getLogger().info("§e> Сообщения загружены");
        }
    }
    
    public static void loadNotifies() {
    	notify_blockedcommand = RGBcolors.translate(messages.getString("notify.blockedcommand"));
    	notify_blockedsymbol = messages.getString("notify.blockedsymbol");
    	notify_blockedsignsymbol = RGBcolors.translate(messages.getString("notify.blockedsignsymbol"));
    	notify_blockedchatsymbol = RGBcolors.translate(messages.getString("notify.blockedchatsymbol"));
    	notify_blockedword = RGBcolors.translate(messages.getString("notify.blockedword"));
    	notify_blocksyntax = RGBcolors.translate(messages.getString("notify.blocksyntax"));
    	notify_maxnumbers = RGBcolors.translate(messages.getString("notify.maxnumbers"));
    	if (instance.debug) {
            instance.getLogger().info("§e> Оповещения загружены");
        }
    }
    
    public static void loadTitles() {
    	titles_blockedcommand = RGBcolors.translate(messages.getString("titles.blockedcommand"));
    	titles_blockedsymbol = RGBcolors.translate(messages.getString("titles.blockedsymbol"));
    	titles_blockedsignsymbol = RGBcolors.translate(messages.getString("titles.blockedsignsymbol"));
    	titles_blockedchatsymbol = RGBcolors.translate(messages.getString("titles.blockedchatsymbol"));
    	titles_blockedword = RGBcolors.translate(messages.getString("titles.blockedword"));
    	titles_blocksyntax = RGBcolors.translate(messages.getString("titles.blocksyntax"));
    	titles_maxnumbers = RGBcolors.translate(messages.getString("titles.maxnumbers"));
    	if (instance.debug) {
            instance.getLogger().info("§e> Тайтлы загружены");
        }
    }
    
    public static void setupSounds(FileConfiguration config) {
    	sounds_admin_notify_sound = Sound.valueOf(config.getString("sounds.admin-notify.sound"));
    	sounds_admin_notify_volume = (float)config.getDouble("sounds.admin-notify.volume");
    	sounds_admin_notify_pitch = (float)config.getDouble("sounds.admin-notify.pitch");
    	sounds_blocked_command_sound = Sound.valueOf(config.getString("sounds.blocked-command.sound"));
    	sounds_blocked_command_volume = (float)config.getDouble("sounds.blocked-command.volume");
    	sounds_blocked_command_pitch = (float)config.getDouble("sounds.blocked-command.pitch");
    	sounds_blocked_symbol_sound = Sound.valueOf(config.getString("sounds.blocked-symbol.sound"));
    	sounds_blocked_symbol_volume = (float)config.getDouble("sounds.blocked-symbol.volume");
    	sounds_blocked_symbol_pitch = (float)config.getDouble("sounds.blocked-symbol.pitch");
    	sounds_blocked_chat_sound = Sound.valueOf(config.getString("sounds.blocked-chat.sound"));
    	sounds_blocked_chat_volume = (float)config.getDouble("sounds.blocked-chat.volume");
    	sounds_blocked_chat_pitch = (float)config.getDouble("sounds.blocked-chat.pitch");
    	if (instance.debug) {
            instance.getLogger().info("§e> Звуки загружены");
        }
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
