package ru.overwrite.ublocker.configuration;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
import ru.overwrite.ublocker.blockgroups.BlockType;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.Condition;
import ru.overwrite.ublocker.configuration.data.*;
import ru.overwrite.ublocker.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class Config {

    @Getter(AccessLevel.NONE)
    private final UniversalBlocker plugin;

    public Config(UniversalBlocker plugin) {
        this.plugin = plugin;
    }

    private ObjectSet<CommandGroup> commandBlockGroupSet;

    private ObjectSet<SymbolGroup> symbolBlockGroupSet;

    private ObjectSet<CommandGroup> commandHideGroupSet;

    private ObjectSet<String> excludedPlayers;

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

    private ChatCharsSettings chatCharsSettings;

    private void setupChatChars(ConfigurationSection allowedChars) {
        if (isNullSection(allowedChars)) {
            return;
        }

        if (!allowedChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.COLORIZER.colorize(allowedChars.getString("message"));

        ConfigurationSection allowedChatCharsSound = allowedChars.getConfigurationSection("sound");
        boolean enableSounds = allowedChatCharsSound.getBoolean("enable");
        String[] sound = allowedChatCharsSound.getString("value").split(";");

        ConfigurationSection allowedChatCharsNotify = allowedChars.getConfigurationSection("notify");
        boolean notify = allowedChatCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedChatCharsNotify.getString("message"));
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

    private BookCharsSettings bookCharsSettings;

    private void setupBookChars(ConfigurationSection allowedBookChars) {
        if (isNullSection(allowedBookChars)) {
            return;
        }

        if (!allowedBookChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.COLORIZER.colorize(allowedBookChars.getString("message"));

        ConfigurationSection allowedBookCharsSound = allowedBookChars.getConfigurationSection("sound");
        boolean enableSounds = allowedBookCharsSound.getBoolean("enable");
        String[] sound = allowedBookCharsSound.getString("value").split(";");

        ConfigurationSection allowedBookCharsNotify = allowedBookChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedBookCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedBookCharsNotify.getString("message"));
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

    private SignCharsSettings signCharsSettings;

    private void setupSignChars(ConfigurationSection allowedSignChars) {
        if (isNullSection(allowedSignChars)) {
            return;
        }

        if (!allowedSignChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.COLORIZER.colorize(allowedSignChars.getString("message"));

        ConfigurationSection allowedSignCharsSound = allowedSignChars.getConfigurationSection("sound");
        boolean enableSounds = allowedSignCharsSound.getBoolean("enable");
        String[] sound = allowedSignCharsSound.getString("value").split(";");

        ConfigurationSection allowedSignCharsNotify = allowedSignChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedSignCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedSignCharsNotify.getString("message"));
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

    private CommandCharsSettings commandCharsSettings;

    private void setupCommandChars(ConfigurationSection allowedCommandChars) {
        if (isNullSection(allowedCommandChars)) {
            return;
        }

        if (!allowedCommandChars.getBoolean("enable")) {
            return;
        }

        String message = Utils.COLORIZER.colorize(allowedCommandChars.getString("message"));

        ConfigurationSection allowedCommandCharsSound = allowedCommandChars.getConfigurationSection("sound");
        boolean enableSounds = allowedCommandCharsSound.getBoolean("enable");
        String[] sound = allowedCommandCharsSound.getString("value").split(";");

        ConfigurationSection allowedCommandCharsNotify = allowedCommandChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedCommandCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedCommandCharsNotify.getString("message"));
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
        String message = Utils.COLORIZER.colorize(numbersCheck.getString("message"));

        ConfigurationSection numbersCheckSound = numbersCheck.getConfigurationSection("sound");
        boolean enableSounds = numbersCheckSound.getBoolean("enable");
        String[] sound = numbersCheckSound.getString("value").split(";");

        ConfigurationSection numbersCheckNotify = numbersCheck.getConfigurationSection("notify");
        boolean notifyEnabled = numbersCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(numbersCheckNotify.getString("message"));
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
        String message = Utils.COLORIZER.colorize(caseCheck.getString("message"));

        ConfigurationSection caseCheckSound = caseCheck.getConfigurationSection("sound");
        boolean enableSounds = caseCheckSound.getBoolean("enable");
        String[] sound = caseCheckSound.getString("value").split(";");

        ConfigurationSection caseCheckNotify = caseCheck.getConfigurationSection("notify");
        boolean notifyEnabled = caseCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(caseCheckNotify.getString("message"));
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

    private BanWordsSettings banWordsSettings;

    private void setupBanWords(ConfigurationSection banWords) {
        if (isNullSection(banWords)) {
            return;
        }

        if (!banWords.getBoolean("enable")) {
            return;
        }

        BlockType mode;
        ObjectSet<String> banWordsString = new ObjectOpenHashSet<>();
        ObjectSet<Pattern> banWordsPattern = new ObjectOpenHashSet<>();
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
        String message = Utils.COLORIZER.colorize(banWords.getString("message"));

        ConfigurationSection banWordsSound = banWords.getConfigurationSection("sound");
        boolean enableSounds = banWordsSound.getBoolean("enable");
        String[] soundValue = banWordsSound.getString("value").split(";");

        ConfigurationSection banWordsNotify = banWords.getConfigurationSection("notify");
        boolean notifyEnabled = banWordsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(banWordsNotify.getString("message"));
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
        commandHideGroupSet = new ObjectOpenHashSet<>();
        for (String commandsID : commands.getConfigurationSection("commands").getKeys(false)) {
            final ConfigurationSection section = commands.getConfigurationSection("commands." + commandsID);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            boolean blockAliases = section.getBoolean("block_aliases") && blockType.equals(BlockType.STRING); // Не будет работать с паттернами
            List<Condition> conditionList = getConditionList(section.getStringList("conditions"));
            List<Action> actionList = getActionList(section.getStringList("actions"));
            commandBlockGroupSet.add(
                    new CommandGroup(
                            commandsID,
                            blockType,
                            blockAliases,
                            new ObjectArrayList<>(section.getStringList("commands")),
                            conditionList,
                            actionList
                    )
            );
            if (!blockType.equals(BlockType.STRING)) {
                return;
            }
            boolean shouldAddToHideList = false;
            for (Action a : actionList) {
                if (a.type() == ActionType.HIDE || a.type() == ActionType.LITE_HIDE) {
                    shouldAddToHideList = true;
                    break;
                }
            }
            if (shouldAddToHideList) {
                ObjectList<String> commandList = new ObjectArrayList<>();
                for (String command : section.getStringList("commands")) {
                    String newCmd = command.replace("/", "");
                    commandList.add(newCmd);
                }
                commandHideGroupSet.add(
                        new CommandGroup(
                                commandsID,
                                blockType,
                                blockAliases,
                                commandList,
                                conditionList,
                                actionList
                        )
                );
            }
        }
    }

    public void setupSymbols(String path) {
        final FileConfiguration symbols = getFile(path, "symbols.yml");
        symbolBlockGroupSet = new ObjectOpenHashSet<>();
        for (String symbolsID : symbols.getConfigurationSection("symbols").getKeys(false)) {
            final ConfigurationSection section = symbols.getConfigurationSection("symbols." + symbolsID);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            ObjectList<String> blockFactor = getBlockFactorList(section.getString("block_factor", ""));
            List<Condition> conditionList = getConditionList(section.getStringList("conditions"));
            List<Action> actionList = getActionList(section.getStringList("actions"));
            symbolBlockGroupSet.add(
                    new SymbolGroup(
                            symbolsID,
                            blockType,
                            blockFactor,
                            new ObjectArrayList<>(section.getStringList("symbols")),
                            new ObjectArrayList<>(section.getStringList("excluded_commands")),
                            conditionList,
                            actionList
                    )
            );
        }
    }

    private ImmutableList<Action> getActionList(List<String> actionStrings) {
        ObjectList<Action> actionList = new ObjectArrayList<>(actionStrings.size());
        for (String action : actionStrings) {
            actionList.add(Action.fromString(action));
        }
        return ImmutableList.copyOf(actionList);
    }

    private ImmutableList<Condition> getConditionList(List<String> conditionStrings) {
        ObjectList<Condition> conditionList = new ObjectArrayList<>(conditionStrings.size());
        for (String action : conditionStrings) {
            conditionList.add(Condition.fromString(action));
        }
        return ImmutableList.copyOf(conditionList);
    }

    private ObjectList<String> getBlockFactorList(String str) {
        return str.contains(";")
                ? ObjectList.of(str.trim().split(";"))
                : ObjectList.of(str.trim());
    }

    public void setupExcluded(FileConfiguration config) {
        excludedPlayers = new ObjectOpenHashSet<>(config.getStringList("excluded_players"));
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
