package ru.overwrite.ublocker.utils.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.actions.*;
import ru.overwrite.ublocker.blockgroups.BlockType;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.*;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.utils.configuration.data.*;

public class Config {

    private final Main plugin;

    public Config(Main plugin) {
        this.plugin = plugin;
    }

    public Set<CommandGroup> commandBlockGroupSet;

    public Set<SymbolGroup> symbolBlockGroupSet;

    public Map<String, List<Action>> commandHideStringActions;

    public Map<String, List<Condition>> commandHideStringConditions;

    public Set<String> excludedplayers;

    public void setupChat(String path) {
        final FileConfiguration chat = getFile(path, "chat.yml");
        final ConfigurationSection settings = chat.getConfigurationSection("chat_settings");
        setupChatChars(settings.getConfigurationSection("allowed_chat_chars"));
        setupBookChars(settings.getConfigurationSection("allowed_book_chars"));
        setupSignChars(settings.getConfigurationSection("allowed_sign_chars"));
        setupCommandChars(settings.getConfigurationSection("allowed_command_chars"));
        setupNumberCheck(settings.getConfigurationSection("numbers_check"));
        setupCaseCheck(settings.getConfigurationSection("case_check"));
        setupBanWords(settings.getConfigurationSection("ban_words_chat"));
    }

    @Getter
    private ChatCharsSettings chatCharsSettings;

    private void setupChatChars(ConfigurationSection allowedChars) {
        if (isNullSection(allowedChars)) {
            return;
        }

        if (!allowedChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.colorize(allowedChars.getString("message"));

        ConfigurationSection allowedChatCharsSound = allowedChars.getConfigurationSection("sound");
        boolean enableSounds = allowedChatCharsSound.getBoolean("enable");
        String[] sound = allowedChatCharsSound.getString("value").split(";");

        ConfigurationSection allowedChatCharsNotify = allowedChars.getConfigurationSection("notify");
        boolean notify = allowedChatCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(allowedChatCharsNotify.getString("message"));
        boolean notifySounds = allowedChatCharsNotify.getBoolean("sound.enable");
        String[] notifySound = allowedChatCharsNotify.getString("sound.value").split(";");

        BlockType mode;
        String string = "";
        Pattern pattern = null;
        switch (allowedChars.getString("mode").toUpperCase()) {
            case "STRING" -> {
                mode = BlockType.STRING;
                string = allowedChars.getString("pattern");
            }
            case "PATTERN" -> {
                mode = BlockType.PATTERN;
                pattern = Pattern.compile(allowedChars.getString("pattern"));
            }
            default -> throw new IllegalArgumentException("Invalid mode in sign character settings.");
        }

        this.chatCharsSettings = new ChatCharsSettings(
                message,
                enableSounds,
                sound,
                notify,
                notifyMessage,
                notifySounds,
                notifySound,
                mode,
                string,
                pattern
        );
    }


    @Getter
    private BookCharsSettings bookCharsSettings;

    private void setupBookChars(ConfigurationSection allowedBookChars) {
        if (isNullSection(allowedBookChars)) {
            return;
        }

        if (!allowedBookChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.colorize(allowedBookChars.getString("message"));

        ConfigurationSection allowedBookCharsSound = allowedBookChars.getConfigurationSection("sound");
        boolean enableSounds = allowedBookCharsSound.getBoolean("enable");
        String[] sound = allowedBookCharsSound.getString("value").split(";");

        ConfigurationSection allowedBookCharsNotify = allowedBookChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedBookCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(allowedBookCharsNotify.getString("message"));
        boolean notifySoundsEnabled = allowedBookCharsNotify.getBoolean("sound.enable");
        String[] notifySound = allowedBookCharsNotify.getString("sound.value").split(";");

        BlockType mode;
        String string = "";
        Pattern pattern = null;
        switch (allowedBookChars.getString("mode").toUpperCase()) {
            case "STRING" -> {
                mode = BlockType.STRING;
                string = allowedBookChars.getString("pattern");
            }
            case "PATTERN" -> {
                mode = BlockType.PATTERN;
                pattern = Pattern.compile(allowedBookChars.getString("pattern"));
            }
            default -> throw new IllegalArgumentException("Invalid mode in sign character settings.");
        }

        this.bookCharsSettings = new BookCharsSettings(
                message,
                enableSounds,
                sound,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySound,
                mode,
                string,
                pattern
        );
    }


    @Getter
    private SignCharsSettings signCharsSettings;

    private void setupSignChars(ConfigurationSection allowedSignChars) {
        if (isNullSection(allowedSignChars)) {
            return;
        }

        if (!allowedSignChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.colorize(allowedSignChars.getString("message"));

        ConfigurationSection allowedSignCharsSound = allowedSignChars.getConfigurationSection("sound");
        boolean enableSounds = allowedSignCharsSound.getBoolean("enable");
        String[] sound = allowedSignCharsSound.getString("value").split(";");

        ConfigurationSection allowedSignCharsNotify = allowedSignChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedSignCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(allowedSignCharsNotify.getString("message"));
        boolean notifySoundsEnabled = allowedSignCharsNotify.getBoolean("sound.enable");
        String[] notifySound = allowedSignCharsNotify.getString("sound.value").split(";");

        BlockType mode;
        String string = "";
        Pattern pattern = null;
        switch (allowedSignChars.getString("mode").toUpperCase()) {
            case "STRING" -> {
                mode = BlockType.STRING;
                string = allowedSignChars.getString("pattern");
            }
            case "PATTERN" -> {
                mode = BlockType.PATTERN;
                pattern = Pattern.compile(allowedSignChars.getString("pattern"));
            }
            default -> throw new IllegalArgumentException("Invalid mode in sign character settings.");
        }

        this.signCharsSettings = new SignCharsSettings(
                message,
                enableSounds,
                sound,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySound,
                mode,
                string,
                pattern
        );
    }

    @Getter
    private CommandCharsSettings commandCharsSettings;

    private void setupCommandChars(ConfigurationSection allowedCommandChars) {
        if (isNullSection(allowedCommandChars)) {
            return;
        }

        if (!allowedCommandChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.colorize(allowedCommandChars.getString("message"));

        ConfigurationSection allowedCommandCharsSound = allowedCommandChars.getConfigurationSection("sound");
        boolean enableSounds = allowedCommandCharsSound.getBoolean("enable");
        String[] sound = allowedCommandCharsSound.getString("value").split(";");

        ConfigurationSection allowedCommandCharsNotify = allowedCommandChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedCommandCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(allowedCommandCharsNotify.getString("message"));
        boolean notifySoundsEnabled = allowedCommandCharsNotify.getBoolean("sound.enable");
        String[] notifySound = allowedCommandCharsNotify.getString("sound.value").split(";");

        BlockType mode;
        String string = "";
        Pattern pattern = null;
        switch (allowedCommandChars.getString("mode").toUpperCase()) {
            case "STRING" -> {
                mode = BlockType.STRING;
                string = allowedCommandChars.getString("pattern");
            }
            case "PATTERN" -> {
                mode = BlockType.PATTERN;
                pattern = Pattern.compile(allowedCommandChars.getString("pattern"));
            }
            default -> throw new IllegalArgumentException("Invalid mode in sign character settings.");
        }

        this.commandCharsSettings = new CommandCharsSettings(
                message,
                enableSounds,
                sound,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySound,
                mode,
                string,
                pattern
        );
    }

    @Getter
    private NumberCheckSettings numberCheckSettings;

    private void setupNumberCheck(ConfigurationSection numbersCheck) {
        if (isNullSection(numbersCheck)) {
            return;
        }

        if (!numbersCheck.getBoolean("enable")) {
            return;
        }

        int maxNumbers = numbersCheck.getInt("maxmsgnumbers");
        boolean strictCheck = numbersCheck.getBoolean("strict");
        String message = Utils.colorize(numbersCheck.getString("message"));

        ConfigurationSection numbersCheckSound = numbersCheck.getConfigurationSection("sound");
        boolean enableSounds = numbersCheckSound.getBoolean("enable");
        String[] sound = numbersCheckSound.getString("value").split(";");

        ConfigurationSection numbersCheckNotify = numbersCheck.getConfigurationSection("notify");
        boolean notifyEnabled = numbersCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(numbersCheckNotify.getString("message"));
        boolean notifySoundsEnabled = numbersCheckNotify.getBoolean("sound.enable");
        String[] notifySound = numbersCheckNotify.getString("sound.value").split(";");

        this.numberCheckSettings = new NumberCheckSettings(
                maxNumbers,
                strictCheck,
                message,
                enableSounds,
                sound,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySound
        );
    }

    @Getter
    private CaseCheckSettings caseCheckSettings;

    private void setupCaseCheck(ConfigurationSection caseCheck) {
        if (isNullSection(caseCheck)) {
            return;
        }

        if (!caseCheck.getBoolean("enable")) {
            return;
        }

        int maxUpperCasePercent = caseCheck.getInt("maxuppercasepercent");
        boolean strictCheck = caseCheck.getBoolean("strict");
        String message = Utils.colorize(caseCheck.getString("message"));

        ConfigurationSection caseCheckSound = caseCheck.getConfigurationSection("sound");
        boolean enableSounds = caseCheckSound.getBoolean("enable");
        String[] sound = caseCheckSound.getString("value").split(";");

        ConfigurationSection caseCheckNotify = caseCheck.getConfigurationSection("notify");
        boolean notifyEnabled = caseCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(caseCheckNotify.getString("message"));
        boolean notifySoundsEnabled = caseCheckNotify.getBoolean("sound.enable");
        String[] notifySound = caseCheckNotify.getString("sound.value").split(";");

        this.caseCheckSettings = new CaseCheckSettings(
                maxUpperCasePercent,
                strictCheck,
                message,
                enableSounds,
                sound,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySound
        );
    }

    @Getter
    private BanWordsSettings banWordsSettings;

    private void setupBanWords(ConfigurationSection banWords) {
        if (isNullSection(banWords)) {
            return;
        }

        if (!banWords.getBoolean("enable")) {
            return;
        }

        BlockType mode;
        Set<String> banWordsString = new ObjectOpenHashSet<>();
        Set<Pattern> banWordsPattern = new ObjectOpenHashSet<>();
        switch (banWords.getString("mode").toUpperCase()) {
            case "STRING":
                mode = BlockType.STRING;
                banWordsString.addAll(banWords.getStringList("words"));
                break;
            case "PATTERN":
                mode = BlockType.PATTERN;
                for (String patternString : banWords.getStringList("words")) {
                    Pattern pattern = Pattern.compile(patternString);
                    banWordsPattern.add(pattern);
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid mode in ban words configuration.");
        }

        boolean block = banWords.getBoolean("block");
        String message = Utils.colorize(banWords.getString("message"));

        ConfigurationSection banWordsSound = banWords.getConfigurationSection("sound");
        boolean enableSounds = banWordsSound.getBoolean("enable");
        String[] soundValue = banWordsSound.getString("value").split(";");

        ConfigurationSection banWordsNotify = banWords.getConfigurationSection("notify");
        boolean notifyEnabled = banWordsNotify.getBoolean("enable");
        String notifyMessage = Utils.colorize(banWordsNotify.getString("message"));
        boolean notifySoundsEnabled = banWordsNotify.getBoolean("sound.enable");
        String[] notifySoundValue = banWordsNotify.getString("sound.value").split(";");

        this.banWordsSettings = new BanWordsSettings(
                mode,
                banWordsString,
                banWordsPattern,
                block,
                message,
                enableSounds,
                soundValue,
                notifyEnabled,
                notifyMessage,
                notifySoundsEnabled,
                notifySoundValue
        );
    }


    private boolean isNullSection(ConfigurationSection section) {
        return section == null;
    }

    public void setupCommands(String path) {
        final FileConfiguration commands = getFile(path, "commands.yml");
        commandBlockGroupSet = new ObjectOpenHashSet<>();
        commandHideStringConditions = new Object2ObjectOpenHashMap<>();
        commandHideStringActions = new Object2ObjectOpenHashMap<>();
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
                    commandHideStringConditions.put(newCmd, conditionList);
                    commandHideStringActions.put(newCmd, actionList);
                }
            }
        }
    }

    public void setupSymbols(String path) {
        final FileConfiguration symbols = getFile(path, "symbols.yml");
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
