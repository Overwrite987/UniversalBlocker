package ru.Overwrite.noCmd.utils;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.noCmd.Main;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

public class Config {
	
	private final Main plugin;
	
	public Config(Main plugin) {
        this.plugin = plugin;
    }
	
	public FileConfiguration messages;
	
	public Set<String> 
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
	
	public String 
	allowedchars,
	allowedbookchars,
	messages_blockedcommand,
	messages_blockedsymbol,
	messages_blockedsignsymbol,
	messages_blockedchatsymbol,
	messages_blockedbooksymbol,
	messages_blockedword,
	messages_blocksyntax,
	messages_maxnumbers,
	notify_blockedcommand,
	notify_blockedsymbol,
	notify_blockedsignsymbol,
	notify_blockedchatsymbol,
	notify_blockedbooksymbol,
	notify_blockedword,
	notify_blocksyntax,
	notify_maxnumbers,
	titles_blockedcommand,
	titles_blockedsymbol,
	titles_blockedsignsymbol,
	titles_blockedchatsymbol,
	titles_blockedbooksymbol,
	titles_blockedword,
	titles_blocksyntax,
	titles_maxnumbers;
	
	public Sound
	sounds_admin_notify_sound,
	sounds_blocked_command_sound,
	sounds_blocked_symbol_sound,
	sounds_blocked_chat_sound,
	sounds_blocked_book_sound;
	
	public boolean
	settings_notify,
	settings_enable_titles,
	settings_enable_sounds,
	chat_settings_strict_number_chek;
	
	public float
	sounds_admin_notify_volume,
	sounds_admin_notify_pitch,
	sounds_blocked_command_volume,
	sounds_blocked_command_pitch,
	sounds_blocked_symbol_volume,
	sounds_blocked_symbol_pitch,
	sounds_blocked_chat_volume,
	sounds_blocked_chat_pitch,
	sounds_blocked_book_volume,
	sounds_blocked_book_pitch;

    public void loadMessageFile() {
        File file = new File(plugin.getDataFolder(), "message.yml");
        if (plugin.getResource("message.yml") == null) {
            save(YamlConfiguration.loadConfiguration(file), "message.yml");
        }
        if (!file.exists()) {
        	plugin.saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        if (plugin.debug) {
        	plugin.logger.info("§a> messages.yml загружен");
        }
    }

    public FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
    public void loadBooleans(FileConfiguration config) {
    	ConfigurationSection settings = config.getConfigurationSection("settings");
    	settings_notify = settings.getBoolean("notify");
    	settings_enable_titles = settings.getBoolean("enable-titles");
    	settings_enable_sounds = settings.getBoolean("enable-sounds");
    	chat_settings_strict_number_chek = config.getBoolean("chat-settings.strict-number-chek");
    	if (plugin.debug) {
            plugin.logger.info("§e> Настройки загружены");
        }
    }
    
    public void loadMessages() {
    	ConfigurationSection msg = messages.getConfigurationSection("messages");
    	messages_blockedcommand = RGBcolors.translate(msg.getString("blockedcommand"));
    	messages_blockedsymbol = RGBcolors.translate(msg.getString("blockedsymbol"));
    	messages_blockedsignsymbol = RGBcolors.translate(msg.getString("blockedsignsymbol"));
    	messages_blockedchatsymbol = RGBcolors.translate(msg.getString("blockedchatsymbol"));
    	messages_blockedbooksymbol = RGBcolors.translate(msg.getString("blockedbooksymbol"));
    	messages_blockedword = RGBcolors.translate(msg.getString("blockedword"));
    	messages_blocksyntax = RGBcolors.translate(msg.getString("blocksyntax"));
    	messages_maxnumbers = RGBcolors.translate(msg.getString("maxnumbers"));
    	if (plugin.debug) {
            plugin.logger.info("§e> Сообщения загружены");
        }
    }
    
    public void loadNotifies() {
    	ConfigurationSection notify = messages.getConfigurationSection("notify");
    	notify_blockedcommand = RGBcolors.translate(notify.getString("blockedcommand"));
    	notify_blockedsymbol = RGBcolors.translate(notify.getString("blockedsymbol"));
    	notify_blockedsignsymbol = RGBcolors.translate(notify.getString("blockedsignsymbol"));
    	notify_blockedchatsymbol = RGBcolors.translate(notify.getString("blockedchatsymbol"));
    	notify_blockedbooksymbol = RGBcolors.translate(notify.getString("blockedbooksymbol"));
    	notify_blockedword = RGBcolors.translate(notify.getString("blockedword"));
    	notify_blocksyntax = RGBcolors.translate(notify.getString("blocksyntax"));
    	notify_maxnumbers = RGBcolors.translate(notify.getString("maxnumbers"));
    	if (plugin.debug) {
            plugin.logger.info("§e> Оповещения загружены");
        }
    }
    
    public void loadTitles() {
    	ConfigurationSection titles = messages.getConfigurationSection("titles");
    	titles_blockedcommand = RGBcolors.translate(titles.getString("blockedcommand"));
    	titles_blockedsymbol = RGBcolors.translate(titles.getString("blockedsymbol"));
    	titles_blockedsignsymbol = RGBcolors.translate(titles.getString("blockedsignsymbol"));
    	titles_blockedbooksymbol = RGBcolors.translate(titles.getString("blockedbooksymbol"));
    	titles_blockedchatsymbol = RGBcolors.translate(titles.getString("blockedchatsymbol"));
    	titles_blockedword = RGBcolors.translate(titles.getString("blockedword"));
    	titles_blocksyntax = RGBcolors.translate(titles.getString("blocksyntax"));
    	titles_maxnumbers = RGBcolors.translate(titles.getString("maxnumbers"));
    	if (plugin.debug) {
            plugin.logger.info("§e> Тайтлы загружены");
        }
    }
    
    public void setupSounds(FileConfiguration config) {
    	ConfigurationSection sounds = config.getConfigurationSection("sounds");
    	sounds_admin_notify_sound = Sound.valueOf(sounds.getString("admin-notify.sound"));
    	sounds_admin_notify_volume = (float)sounds.getDouble("admin-notify.volume");
    	sounds_admin_notify_pitch = (float)sounds.getDouble("admin-notify.pitch");
    	sounds_blocked_command_sound = Sound.valueOf(sounds.getString("blocked-command.sound"));
    	sounds_blocked_command_volume = (float)sounds.getDouble("blocked-command.volume");
    	sounds_blocked_command_pitch = (float)sounds.getDouble("blocked-command.pitch");
    	sounds_blocked_symbol_sound = Sound.valueOf(sounds.getString("blocked-symbol.sound"));
    	sounds_blocked_symbol_volume = (float)sounds.getDouble("blocked-symbol.volume");
    	sounds_blocked_symbol_pitch = (float)sounds.getDouble("blocked-symbol.pitch");
    	sounds_blocked_chat_sound = Sound.valueOf(sounds.getString("blocked-chat.sound"));
    	sounds_blocked_chat_volume = (float)sounds.getDouble("blocked-chat.volume");
    	sounds_blocked_chat_pitch = (float)sounds.getDouble("blocked-chat.pitch");
    	sounds_blocked_book_sound = Sound.valueOf(sounds.getString("blocked-book.sound"));
    	sounds_blocked_book_volume = (float)sounds.getDouble("blocked-book.pitch");
    	sounds_blocked_book_pitch = (float)sounds.getDouble("blocked-book.pitch");
    	if (plugin.debug) {
            plugin.logger.info("§e> Звуки загружены");
        }
    }
    
    public void setupExcluded(FileConfiguration config) {
        excludedplayers = new HashSet<>(config.getStringList("excluded-players"));
        if (plugin.debug) {
            plugin.logger.info("§e> Невосприимчивые игроки загружены");
        }
    }
    
    public void setupCommands(FileConfiguration config) {
    	fullblocked = new HashSet<>(config.getStringList("blocked-commands.full"));
    	liteblocked = new HashSet<>(config.getStringList("blocked-commands.lite"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Списки блокировок команд загружены");
    	}
    }
    
    public void setupBanWords(FileConfiguration config) {
    	banwords = new HashSet<>(config.getStringList("chat-settings.ban-words"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Списки заблокированных слов загружены");
    	}
    }
    
    public void setupChars(FileConfiguration config) {
    	allowedchars = config.getString("chat-settings.allowed-chars");
    	if (plugin.debug) {
    		plugin.logger.info("§e> Список разрешенных символов чата загружен");
    	}
    }
    
    public void setupBookChars(FileConfiguration config) {
    	allowedbookchars = config.getString("chat-settings.allowed-book-chars");
    	if (plugin.debug) {
    		plugin.logger.info("§e> Список разрешенных символов книжек загружен");
    	}
    }
    
    public void setupSyntax(FileConfiguration config) {
    	blockedsymbol = new HashSet<>(config.getStringList("symbols.blocked-symbols"));
    	excludedcommands = new HashSet<>(config.getStringList("symbols.excluded-commands"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Списки блокировок символов команд загружены");
    	}
    }
    
    public void setupSignSyntax(FileConfiguration config) {
    	blockedsignsymbol = new HashSet<>(config.getStringList("symbols.blocked-sign-symbols"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Списки блокировок символов табличек загружены");
    	}
    }
    
    public void setupArgshidden(FileConfiguration config) {
    	argshidedcmds = new HashSet<>(config.getStringList("blocked-commands.args-tab-complete"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Команды с заблокированными аргументами загружены");
    	}
    }
    
    public void setupRcon(FileConfiguration config) {
    	rconblocked = new HashSet<>(config.getStringList("blocked-commands.rcon"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Заблокированные команды rcon загружены");
    	}
    }
    
    public void setupConsole(FileConfiguration config) {
    	consoleblocked = new HashSet<>(config.getStringList("blocked-commands.console"));
    	if (plugin.debug) {
    		plugin.logger.info("§e> Заблокированные команды консоли загружены");
    	}
    }
}
