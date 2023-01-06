package ru.Overwrite.noCmd.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.Overwrite.noCmd.Main;

import java.io.File;
import java.io.IOException;

public class Config {
	
	public static FileConfiguration messages;

    public static void loadMessages() {
        File file = new File(Main.getInstance().getDataFolder(), "message.yml");
        if (Main.getInstance().getResource("message.yml") == null) {
            save(YamlConfiguration.loadConfiguration(file), "message.yml");
        }
        if (!file.exists()) {
        	Main.getInstance().saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        Main.getInstance().getLogger().info("messages.yml загружен");
    }

    public static FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(Main.getInstance().getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
    
}
