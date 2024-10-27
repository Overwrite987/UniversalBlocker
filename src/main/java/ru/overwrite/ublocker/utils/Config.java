package ru.overwrite.ublocker.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.*;
import ru.overwrite.ublocker.blockgroups.BlockType;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.*;
import ru.overwrite.ublocker.listeners.chat.*;

public class Config {

    private final Main plugin;

    public Config(Main plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration chat;
    public FileConfiguration symbols;
    public FileConfiguration commands;

    public Set<CommandGroup> commandBlockGroupSet;
    public Set<SymbolGroup> symbolBlockGroupSet;

    public Set<String> ban_words_string;
    public Set<Pattern> ban_words_pattern;

    public Map<String, List<Action>> commandHide_string_actions;

    public Map<String, List<Condition>> commandHide_string_conditions;

    public BlockType allowed_chat_chars_mode, allowed_book_chars_mode, allowed_sign_chars_mode, allowed_command_chars_mode, banwordmode;

    public String allowed_chat_chars_message, allowed_chat_chars_sound_id,
            allowed_chat_chars_notify_message, allowed_chat_chars_notify_sound_id, allowed_chat_chars_string,
            allowed_book_chars_message, allowed_book_chars_sound_id,
            allowed_book_chars_notify_message, allowed_book_chars_notify_sound_id, allowed_book_chars_string,
            allowed_sign_chars_message, allowed_sign_chars_sound_id,
            allowed_sign_chars_notify_message, allowed_sign_chars_notify_sound_id, allowed_sign_chars_string,
            allowed_command_chars_message, allowed_command_chars_sound_id,
            allowed_command_chars_notify_message, allowed_command_chars_notify_sound_id, allowed_command_chars_string,
            numbers_check_message, numbers_check_sound_id, numbers_check_notify_message, numbers_check_notify_sound_id,
            case_check_message, case_check_sound_id, case_check_notify_message, case_check_notify_sound_id,
            ban_words_message, ban_words_sound_id, ban_words_notify_message, ban_words_notify_sound_id;

    public Pattern allowed_chat_chars_pattern, allowed_book_chars_pattern, allowed_sign_chars_pattern,
            allowed_command_chars_pattern;

    public boolean allowed_chat_chars_notify, allowed_chat_chars_enable_sounds, allowed_chat_chars_notify_sounds,
            allowed_book_chars_notify, allowed_book_chars_enable_sounds, allowed_book_chars_notify_sounds,
            allowed_sign_chars_notify, allowed_sign_chars_enable_sounds, allowed_sign_chars_notify_sounds,
            allowed_command_chars_notify, allowed_command_chars_enable_sounds, allowed_command_chars_notify_sounds,
            numbers_check_enable_sounds, strict_numbers_check, numbers_check_notify, numbers_check_notify_sounds,
            case_check_enable_sounds, strict_case_check, case_check_notify, case_check_notify_sounds,
            ban_words_block, ban_words_enable_sounds, ban_words_notify, ban_words_notify_sounds;

    public Set<String> excludedplayers;

    public float allowed_chat_chars_sound_volume, allowed_chat_chars_sound_pitch,
            allowed_chat_chars_notify_sound_volume, allowed_chat_chars_notify_sound_pitch,
            allowed_book_chars_sound_volume, allowed_book_chars_sound_pitch, allowed_book_chars_notify_sound_volume,
            allowed_book_chars_notify_sound_pitch, allowed_sign_chars_sound_volume, allowed_sign_chars_sound_pitch,
            allowed_sign_chars_notify_sound_volume, allowed_sign_chars_notify_sound_pitch,
            allowed_command_chars_sound_volume, allowed_command_chars_sound_pitch,
            allowed_command_chars_notify_sound_volume, allowed_command_chars_notify_sound_pitch,
            numbers_check_sound_volume, numbers_check_sound_pitch, numbers_check_notify_sound_volume,
            numbers_check_notify_sound_pitch, case_check_sound_volume, case_check_sound_pitch,
            case_check_notify_sound_volume, case_check_notify_sound_pitch, ban_words_sound_volume, ban_words_sound_pitch,
            ban_words_notify_sound_volume, ban_words_notify_sound_pitch;

    public int maxmsgnumbers, maxcuppercasepercent;

    public void setupChat(String path) {
        chat = getFile(path, "chat.yml");
        final ConfigurationSection settings = chat.getConfigurationSection("chat_settings");
        setupChatChars(settings.getConfigurationSection("allowed_chat_chars"));
        setupBookChars(settings.getConfigurationSection("allowed_book_chars"));
        setupSignChars(settings.getConfigurationSection("allowed_sign_chars"));
        setupCommandChars(settings.getConfigurationSection("allowed_command_chars"));
        setupNumberCheck(settings.getConfigurationSection("numbers_check"));
        setupCaseCheck(settings.getConfigurationSection("case_check"));
        setupBanWords(settings.getConfigurationSection("ban_words_chat"));
    }

    private void setupChatChars(ConfigurationSection allowedChars) {
        if (isNullSection(allowedChars)) {
            return;
        }
        if (allowedChars.getBoolean("enable")) {
            ChatFilter.enabled = true;
            allowed_chat_chars_message = Utils.colorize(allowedChars.getString("message"));
            final ConfigurationSection allowedChatCharsSound = allowedChars.getConfigurationSection("sound");
            allowed_chat_chars_enable_sounds = allowedChatCharsSound.getBoolean("enable");
            String[] blockedSoundValue = allowedChatCharsSound.getString("value").split(";");
            allowed_chat_chars_sound_id = blockedSoundValue[0];
            allowed_chat_chars_sound_volume = Float.valueOf(blockedSoundValue[1]);
            allowed_chat_chars_sound_pitch = Float.valueOf(blockedSoundValue[2]);
            final ConfigurationSection allowedChatCharsNotify = allowedChars.getConfigurationSection("notify");
            allowed_chat_chars_notify = allowedChatCharsNotify.getBoolean("enable");
            allowed_chat_chars_notify_message = Utils.colorize(allowedChatCharsNotify.getString("message"));
            allowed_chat_chars_notify_sounds = allowedChatCharsNotify.getBoolean("sound.enable");
            String[] notifySoundValue = allowedChatCharsNotify.getString("sound.value").split(";");
            allowed_chat_chars_notify_sound_id = notifySoundValue[0];
            allowed_chat_chars_notify_sound_volume = Float.valueOf(notifySoundValue[1]);
            allowed_chat_chars_notify_sound_pitch = Float.valueOf(notifySoundValue[2]);
            switch (allowedChars.getString("mode").toUpperCase()) {
                case "STRING": {
                    allowed_chat_chars_mode = BlockType.STRING;
                    allowed_chat_chars_string = allowedChars.getString("pattern");
                    break;
                }
                case "PATTERN": {
                    allowed_chat_chars_mode = BlockType.PATTERN;
                    allowed_chat_chars_pattern = Pattern.compile(allowedChars.getString("pattern"));
                    break;
                }
            }
        }
    }

    private void setupBookChars(ConfigurationSection allowedBookChars) {
        if (isNullSection(allowedBookChars)) {
            return;
        }
        if (allowedBookChars.getBoolean("enable")) {
            BookChecker.enabled = true;
            allowed_book_chars_message = Utils.colorize(allowedBookChars.getString("message"));
            final ConfigurationSection allowedBookCharsSound = allowedBookChars.getConfigurationSection("sound");
            allowed_book_chars_enable_sounds = allowedBookCharsSound.getBoolean("enable");
            String[] blockedSoundValue = allowedBookCharsSound.getString("value").split(";");
            allowed_book_chars_sound_id = blockedSoundValue[0];
            allowed_book_chars_sound_volume = Float.valueOf(blockedSoundValue[1]);
            allowed_book_chars_sound_pitch = Float.valueOf(blockedSoundValue[2]);
            final ConfigurationSection allowedBookCharsNotify = allowedBookChars.getConfigurationSection("notify");
            allowed_book_chars_notify = allowedBookCharsNotify.getBoolean("enable");
            allowed_book_chars_notify_message = Utils.colorize(allowedBookCharsNotify.getString("message"));
            allowed_book_chars_notify_sounds = allowedBookCharsNotify.getBoolean("sound.enable");
            String[] notifySoundValue = allowedBookCharsNotify.getString("sound.value").split(";");
            allowed_book_chars_notify_sound_id = notifySoundValue[0];
            allowed_book_chars_notify_sound_volume = Float.valueOf(notifySoundValue[1]);
            allowed_book_chars_notify_sound_pitch = Float.valueOf(notifySoundValue[2]);
            switch (allowedBookChars.getString("mode").toUpperCase()) {
                case "STRING": {
                    allowed_book_chars_mode = BlockType.STRING;
                    allowed_book_chars_string = allowedBookChars.getString("pattern");
                    break;
                }
                case "PATTERN": {
                    allowed_book_chars_mode = BlockType.PATTERN;
                    allowed_book_chars_pattern = Pattern.compile(allowedBookChars.getString("pattern"));
                    break;
                }
            }
        }
    }

    private void setupSignChars(ConfigurationSection allowedSignChars) {
        if (isNullSection(allowedSignChars)) {
            return;
        }
        if (allowedSignChars.getBoolean("enable")) {
            SignFilter.enabled = true;
            allowed_sign_chars_message = Utils.colorize(allowedSignChars.getString("message"));
            final ConfigurationSection allowedSignCharsSound = allowedSignChars.getConfigurationSection("sound");
            allowed_sign_chars_enable_sounds = allowedSignCharsSound.getBoolean("enable");
            String[] blockedSoundValue = allowedSignCharsSound.getString("value").split(";");
            allowed_sign_chars_sound_id = blockedSoundValue[0];
            allowed_sign_chars_sound_volume = Float.parseFloat(blockedSoundValue[1]);
            allowed_sign_chars_sound_pitch = Float.parseFloat(blockedSoundValue[2]);
            final ConfigurationSection allowedSignCharsNotify = allowedSignChars.getConfigurationSection("notify");
            allowed_sign_chars_notify = allowedSignCharsNotify.getBoolean("enable");
            allowed_sign_chars_notify_message = Utils.colorize(allowedSignCharsNotify.getString("message"));
            allowed_sign_chars_notify_sounds = allowedSignCharsNotify.getBoolean("sound.enable");
            String[] notifySoundValue = allowedSignCharsNotify.getString("sound.value").split(";");
            allowed_sign_chars_notify_sound_id = notifySoundValue[0];
            allowed_sign_chars_notify_sound_volume = Float.parseFloat(notifySoundValue[1]);
            allowed_sign_chars_notify_sound_pitch = Float.parseFloat(notifySoundValue[2]);
            switch (allowedSignChars.getString("mode").toUpperCase()) {
                case "STRING": {
                    allowed_sign_chars_mode = BlockType.STRING;
                    allowed_sign_chars_string = allowedSignChars.getString("pattern");
                    break;
                }
                case "PATTERN": {
                    allowed_sign_chars_mode = BlockType.PATTERN;
                    allowed_sign_chars_pattern = Pattern.compile(allowedSignChars.getString("pattern"));
                    break;
                }
            }
        }
    }

    private void setupCommandChars(ConfigurationSection allowedCommandChars) {
        if (isNullSection(allowedCommandChars)) {
            return;
        }
        if (allowedCommandChars.getBoolean("enable")) {
            CommandFilter.enabled = true;
            allowed_command_chars_message = Utils.colorize(allowedCommandChars.getString("message"));
            final ConfigurationSection allowedCommandCharsSound = allowedCommandChars.getConfigurationSection("sound");
            allowed_command_chars_enable_sounds = allowedCommandCharsSound.getBoolean("enable");
            String[] blockedSoundValue = allowedCommandCharsSound.getString("value").split(";");
            allowed_command_chars_sound_id = blockedSoundValue[0];
            allowed_command_chars_sound_volume = Float.parseFloat(blockedSoundValue[1]);
            allowed_command_chars_sound_pitch = Float.parseFloat(blockedSoundValue[2]);
            final ConfigurationSection allowedCommandCharsNotify = allowedCommandChars.getConfigurationSection("notify");
            allowed_command_chars_notify = allowedCommandCharsNotify.getBoolean("enable");
            allowed_command_chars_notify_message = Utils.colorize(allowedCommandCharsNotify.getString("message"));
            allowed_command_chars_notify_sounds = allowedCommandCharsNotify.getBoolean("sound.enable");
            String[] notifySoundValue = allowedCommandCharsNotify.getString("sound.value").split(";");
            allowed_command_chars_notify_sound_id = notifySoundValue[0];
            allowed_command_chars_notify_sound_volume = Float.parseFloat(notifySoundValue[1]);
            allowed_command_chars_notify_sound_pitch = Float.parseFloat(notifySoundValue[2]);
            switch (allowedCommandChars.getString("mode").toUpperCase()) {
                case "STRING": {
                    allowed_command_chars_mode = BlockType.STRING;
                    allowed_command_chars_string = allowedCommandChars.getString("pattern");
                    break;
                }
                case "PATTERN": {
                    allowed_command_chars_mode = BlockType.PATTERN;
                    allowed_command_chars_pattern = Pattern.compile(allowedCommandChars.getString("pattern"));
                    break;
                }
            }
        }
    }

    private void setupNumberCheck(ConfigurationSection numbersCheck) {
        if (isNullSection(numbersCheck)) {
            return;
        }
        if (numbersCheck.getBoolean("enable")) {
            NumbersCheck.enabled = true;
            maxmsgnumbers = numbersCheck.getInt("maxmsgnumbers");
            strict_numbers_check = numbersCheck.getBoolean("strict");
            numbers_check_message = Utils.colorize(numbersCheck.getString("message"));
            final ConfigurationSection numbersCheckSound = numbersCheck.getConfigurationSection("sound");
            numbers_check_enable_sounds = numbersCheckSound.getBoolean("enable");
            String[] blockedSoundValue = numbersCheckSound.getString("value").split(";");
            numbers_check_sound_id = blockedSoundValue[0];
            numbers_check_sound_volume = Float.parseFloat(blockedSoundValue[1]);
            numbers_check_sound_pitch = Float.parseFloat(blockedSoundValue[2]);
            final ConfigurationSection numbersCheckNotify = numbersCheck.getConfigurationSection("notify");
            numbers_check_notify = numbersCheckNotify.getBoolean("enable");
            numbers_check_notify_message = Utils.colorize(numbersCheckNotify.getString("message"));
            numbers_check_notify_sounds = numbersCheckNotify.getBoolean("sound.enable");
            String[] notifySoundValue = numbersCheckNotify.getString("sound.value").split(";");
            numbers_check_notify_sound_id = notifySoundValue[0];
            numbers_check_notify_sound_volume = Float.parseFloat(notifySoundValue[1]);
            numbers_check_notify_sound_pitch = Float.parseFloat(notifySoundValue[2]);
        }
    }

    private void setupCaseCheck(ConfigurationSection caseCheck) {
        if (isNullSection(caseCheck)) {
            return;
        }
        if (caseCheck.getBoolean("enable")) {
            CaseCheck.enabled = true;
            maxcuppercasepercent = caseCheck.getInt("maxcuppercasepercent");
            strict_case_check = caseCheck.getBoolean("strict");
            case_check_message = Utils.colorize(caseCheck.getString("message"));
            final ConfigurationSection caseCheckSound = caseCheck.getConfigurationSection("sound");
            case_check_enable_sounds = caseCheckSound.getBoolean("enable");
            String[] blockedSoundValue = caseCheckSound.getString("value").split(";");
            case_check_sound_id = blockedSoundValue[0];
            case_check_sound_volume = Float.parseFloat(blockedSoundValue[1]);
            case_check_sound_pitch = Float.parseFloat(blockedSoundValue[2]);
            final ConfigurationSection caseCheckNotify = caseCheck.getConfigurationSection("notify");
            case_check_notify = caseCheckNotify.getBoolean("enable");
            case_check_notify_message = Utils.colorize(caseCheckNotify.getString("message"));
            case_check_notify_sounds = caseCheckNotify.getBoolean("sound.enable");
            String[] notifySoundValue = caseCheckNotify.getString("sound.value").split(";");
            case_check_notify_sound_id = notifySoundValue[0];
            case_check_notify_sound_volume = Float.parseFloat(notifySoundValue[1]);
            case_check_notify_sound_pitch = Float.parseFloat(notifySoundValue[2]);
        }
    }

    private void setupBanWords(ConfigurationSection banWords) {
        if (isNullSection(banWords)) {
            return;
        }
        if (banWords.getBoolean("enable")) {
            BanWords.enabled = true;
            switch (banWords.getString("mode").toUpperCase()) {
                case "STRING": {
                    banwordmode = BlockType.STRING;
                    ban_words_string = new ObjectOpenHashSet<>(banWords.getStringList("words"));
                    break;
                }
                case "PATTERN": {
                    banwordmode = BlockType.PATTERN;
                    ban_words_pattern = new ObjectOpenHashSet<>();
                    for (String patternString : banWords.getStringList("words")) {
                        Pattern pattern = Pattern.compile(patternString);
                        ban_words_pattern.add(pattern);
                    }
                }
            }
            ban_words_block = banWords.getBoolean("block");
            ban_words_message = Utils.colorize(banWords.getString("message"));
            final ConfigurationSection banWordsSound = banWords.getConfigurationSection("sound");
            ban_words_enable_sounds = banWordsSound.getBoolean("enable");
            String[] blockedSoundValue = banWordsSound.getString("value").split(";");
            ban_words_sound_id = blockedSoundValue[0];
            ban_words_sound_volume = Float.parseFloat(blockedSoundValue[1]);
            ban_words_sound_pitch = Float.parseFloat(blockedSoundValue[2]);
            final ConfigurationSection banWordsNotify = banWords.getConfigurationSection("notify");
            ban_words_notify = banWordsNotify.getBoolean("enable");
            ban_words_notify_message = Utils.colorize(banWordsNotify.getString("message"));
            ban_words_notify_sounds = banWordsNotify.getBoolean("sound.enable");
            String[] notifySoundValue = banWordsNotify.getString("sound.value").split(";");
            ban_words_notify_sound_id = notifySoundValue[0];
            ban_words_notify_sound_volume = Float.parseFloat(notifySoundValue[1]);
            ban_words_notify_sound_pitch = Float.parseFloat(notifySoundValue[2]);
        }
    }

    private boolean isNullSection(ConfigurationSection section) {
        return section == null;
    }

    public void setupCommands(String path) {
        commands = getFile(path, "commands.yml");
        commandBlockGroupSet = new ObjectOpenHashSet<>();
        commandHide_string_conditions = new Object2ObjectOpenHashMap<>();
        commandHide_string_actions = new Object2ObjectOpenHashMap<>();
        for (String cmds : commands.getConfigurationSection("commands").getKeys(false)) {
            final ConfigurationSection section = commands.getConfigurationSection("commands." + cmds);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            List<Condition> conditionList = new ArrayList<>();
            for (String s : section.getStringList("conditions")) {
                conditionList.add(Condition.fromString(s));
            }
            List<Action> actionList = new ArrayList<>();
            for (String s : section.getStringList("actions")) {
                actionList.add(Action.fromString(s));
            }
            commandBlockGroupSet.add(new CommandGroup(cmds, blockType, section.getStringList("commands"), conditionList, actionList));
            boolean shouldAddToHideList = false;
            for (Action a : actionList) {
                if (a.type() == ActionType.HIDE || a.type() == ActionType.LITE_HIDE) {
                    shouldAddToHideList = true;
                    break;
                }
            }
            if (shouldAddToHideList) {
                for (String s : section.getStringList("commands")) {
                    String newCmd = s.replace("/", "");
                    commandHide_string_conditions.put(newCmd, conditionList);
                    commandHide_string_actions.put(newCmd, actionList);
                }
            }
        }
    }

    public void setupSymbols(String path) {
        symbols = getFile(path, "symbols.yml");
        symbolBlockGroupSet = new ObjectOpenHashSet<>();
        for (String smbls : symbols.getConfigurationSection("symbols").getKeys(false)) {
            final ConfigurationSection section = symbols.getConfigurationSection("symbols." + smbls);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            String[] blockFactor = section.getString("block_factor", "").split(";");
            List<Condition> conditionList = new ArrayList<>();
            for (String s : section.getStringList("conditions")) {
                conditionList.add(Condition.fromString(s));
            }
            List<Action> actionList = new ArrayList<>();
            for (String s : section.getStringList("actions")) {
                actionList.add(Action.fromString(s));
            }
            symbolBlockGroupSet.add(new SymbolGroup(smbls, blockType, blockFactor, section.getStringList("symbols"), section.getStringList("excluded_commands"), conditionList, actionList));
        }
    }

    public void setupExcluded(FileConfiguration config) {
        excludedplayers = new ObjectOpenHashSet<>(config.getStringList("excluded_players"));
    }

    public FileConfiguration getFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration save(FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }
}
