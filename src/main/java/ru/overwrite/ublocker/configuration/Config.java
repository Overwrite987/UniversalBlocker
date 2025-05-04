package ru.overwrite.ublocker.configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
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
import ru.overwrite.ublocker.listeners.chat.*;
import ru.overwrite.ublocker.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
public class Config {

    @Getter(AccessLevel.NONE)
    private final UniversalBlocker plugin;

    public Config(UniversalBlocker plugin) {
        this.plugin = plugin;
    }

    private Set<CommandGroup> commandBlockGroupSet;

    private Set<SymbolGroup> symbolBlockGroupSet;

    private Set<CommandGroup> commandHideGroupSet;

    private Set<String> excludedPlayers;

    public void setupChat(String path) {
        final FileConfiguration chat = getFile(path, "chat.yml");
        final ConfigurationSection settings = chat.getConfigurationSection("chat_settings");
        setupChatChars(settings.getConfigurationSection("allowed_chat_chars"));
        setupBookChars(settings.getConfigurationSection("allowed_book_chars"));
        setupSignChars(settings.getConfigurationSection("allowed_sign_chars"));
        setupCommandChars(settings.getConfigurationSection("allowed_command_chars"));
        setupNumberCheck(settings.getConfigurationSection("numbers_check"));
        setupCaseCheck(settings.getConfigurationSection("case_check"));
        setupSameMessages(settings.getConfigurationSection("same_messages"));
        setupBanWords(settings.getConfigurationSection("ban_words_chat"));
    }

    private ChatCharsSettings chatCharsSettings;

    private void setupChatChars(ConfigurationSection allowedChars) {
        if (isNullSection(allowedChars)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(ChatFilter.class.getSimpleName());

        boolean shouldBeRegistered = allowedChars.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        String message = Utils.COLORIZER.colorize(allowedChars.getString("message"));
        String[] sound = allowedChars.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection allowedChatCharsNotify = allowedChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedChatCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedChatCharsNotify.getString("message"));
        String[] notifySound = allowedChatCharsNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        BlockType mode = BlockType.valueOf(allowedChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedChars.getString("pattern"));
        }

        this.chatCharsSettings = new ChatCharsSettings(
                mode,
                charSet,
                pattern,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private BookCharsSettings bookCharsSettings;

    private void setupBookChars(ConfigurationSection allowedBookChars) {
        if (isNullSection(allowedBookChars)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(BookFilter.class.getSimpleName());

        boolean shouldBeRegistered = allowedBookChars.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        String message = Utils.COLORIZER.colorize(allowedBookChars.getString("message"));
        String[] sound = allowedBookChars.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection allowedBookCharsNotify = allowedBookChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedBookCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedBookCharsNotify.getString("message"));
        String[] notifySound = allowedBookCharsNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        BlockType mode = BlockType.valueOf(allowedBookChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedBookChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedBookChars.getString("pattern"));
        }

        this.bookCharsSettings = new BookCharsSettings(
                mode,
                charSet,
                pattern,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private SignCharsSettings signCharsSettings;

    private void setupSignChars(ConfigurationSection allowedSignChars) {
        if (isNullSection(allowedSignChars)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(SignFilter.class.getSimpleName());

        boolean shouldBeRegistered = allowedSignChars.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        String message = Utils.COLORIZER.colorize(allowedSignChars.getString("message"));
        String[] sound = allowedSignChars.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection allowedSignCharsNotify = allowedSignChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedSignCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedSignCharsNotify.getString("message"));
        String[] notifySound = allowedSignCharsNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        BlockType mode = BlockType.valueOf(allowedSignChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedSignChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedSignChars.getString("pattern"));
        }

        this.signCharsSettings = new SignCharsSettings(
                mode,
                charSet,
                pattern,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private CommandCharsSettings commandCharsSettings;

    private void setupCommandChars(ConfigurationSection allowedCommandChars) {
        if (isNullSection(allowedCommandChars)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(CommandFilter.class.getSimpleName());

        boolean shouldBeRegistered = allowedCommandChars.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        String message = Utils.COLORIZER.colorize(allowedCommandChars.getString("message"));
        String[] sound = allowedCommandChars.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection allowedCommandCharsNotify = allowedCommandChars.getConfigurationSection("notify");
        boolean notifyEnabled = allowedCommandCharsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(allowedCommandCharsNotify.getString("message"));
        String[] notifySound = allowedCommandCharsNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        BlockType mode = BlockType.valueOf(allowedCommandChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedCommandChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedCommandChars.getString("pattern"));
        }

        this.commandCharsSettings = new CommandCharsSettings(
                mode,
                charSet,
                pattern,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private NumberCheckSettings numberCheckSettings;

    private void setupNumberCheck(ConfigurationSection numbersCheck) {
        if (isNullSection(numbersCheck)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(NumbersCheck.class.getSimpleName());

        boolean shouldBeRegistered = numbersCheck.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        int maxNumbers = numbersCheck.getInt("maxmsgnumbers");
        boolean strictCheck = numbersCheck.getBoolean("strict");

        String message = Utils.COLORIZER.colorize(numbersCheck.getString("message"));
        String[] sound = numbersCheck.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection numbersCheckNotify = numbersCheck.getConfigurationSection("notify");
        boolean notifyEnabled = numbersCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(numbersCheckNotify.getString("message"));
        String[] notifySound = numbersCheckNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        this.numberCheckSettings = new NumberCheckSettings(
                maxNumbers,
                strictCheck,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private CaseCheckSettings caseCheckSettings;

    private void setupCaseCheck(ConfigurationSection caseCheck) {
        if (isNullSection(caseCheck)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(CaseCheck.class.getSimpleName());

        boolean shouldBeRegistered = caseCheck.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        int maxUpperCasePercent = caseCheck.getInt("maxuppercasepercent");
        boolean strictCheck = caseCheck.getBoolean("strict");
        String message = Utils.COLORIZER.colorize(caseCheck.getString("message"));

        String[] sound = caseCheck.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        ConfigurationSection caseCheckNotify = caseCheck.getConfigurationSection("notify");
        boolean notifyEnabled = caseCheckNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(caseCheckNotify.getString("message"));
        String[] notifySound = caseCheckNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        this.caseCheckSettings = new CaseCheckSettings(
                maxUpperCasePercent,
                strictCheck,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private SameMessagesSettings sameMessagesSettings;

    private void setupSameMessages(ConfigurationSection sameMessages) {
        if (isNullSection(sameMessages)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(SameMessageLimiter.class.getSimpleName());

        boolean shouldBeRegistered = sameMessages.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        int samePercents = sameMessages.getInt("same_percents");
        int maxSameMessage = sameMessages.getInt("max_same_message");
        int historySize = sameMessages.getInt("history_size");

        String message = Utils.COLORIZER.colorize(sameMessages.getString("message"));
        String[] sound = sameMessages.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection sameMessagesNotify = sameMessages.getConfigurationSection("notify");
        boolean notifyEnabled = sameMessagesNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(sameMessagesNotify.getString("message"));
        String[] notifySound = sameMessagesNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        this.sameMessagesSettings = new SameMessagesSettings(
                samePercents,
                maxSameMessage,
                historySize,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private BanWordsSettings banWordsSettings;

    private void setupBanWords(ConfigurationSection banWords) {
        if (isNullSection(banWords)) {
            return;
        }

        ChatListener chatListener = plugin.getChatListeners().get(BanWords.class.getSimpleName());

        boolean shouldBeRegistered = banWords.getBoolean("enable");

        if (chatListener.isRegistered() != shouldBeRegistered) {
            chatListener.setRegistered(shouldBeRegistered);
        }

        BlockType mode = BlockType.valueOf(banWords.getString("mode").toUpperCase());
        ObjectSet<String> banWordsString = null;
        ObjectSet<Pattern> banWordsPattern = null;
        switch (mode) {
            case STRING:
                banWordsString = new ObjectOpenHashSet<>();
                banWordsString.addAll(banWords.getStringList("words"));
                break;
            case PATTERN:
                banWordsPattern = new ObjectOpenHashSet<>();
                for (String patternString : banWords.getStringList("words")) {
                    Pattern pattern = Pattern.compile(patternString);
                    banWordsPattern.add(pattern);
                }
                break;
        }

        boolean block = banWords.getBoolean("block");

        String message = Utils.COLORIZER.colorize(banWords.getString("message"));
        String[] sound = banWords.getString("sound", "ENTITY_ITEM_BREAK;1.0;1.0").split(";");

        final ConfigurationSection banWordsNotify = banWords.getConfigurationSection("notify");
        boolean notifyEnabled = banWordsNotify.getBoolean("enable");
        String notifyMessage = Utils.COLORIZER.colorize(banWordsNotify.getString("message"));
        String[] notifySound = banWordsNotify.getString("sound", "BLOCK_NOTE_BLOCK_PLING;1.0;1.0").split(";");

        this.banWordsSettings = new BanWordsSettings(
                mode,
                banWordsString,
                banWordsPattern,
                block,
                new CancellationSettings(
                        message,
                        sound,
                        notifyEnabled,
                        notifyMessage,
                        notifySound
                )
        );
    }

    private boolean isNullSection(ConfigurationSection section) {
        return section == null;
    }

    public void setupCommands(String path) {
        final FileConfiguration commands = getFile(path, "commands.yml");
        ObjectSet<CommandGroup> commandBlockGroupSet = new ObjectOpenHashSet<>();
        ObjectSet<CommandGroup> commandHideGroupSet = new ObjectOpenHashSet<>();
        for (String commandsID : commands.getConfigurationSection("commands").getKeys(false)) {
            final ConfigurationSection section = commands.getConfigurationSection("commands." + commandsID);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            boolean blockAliases = section.getBoolean("block_aliases") && blockType == BlockType.STRING; // Не будет работать с паттернами
            List<Condition> conditionList = getConditionList(section.getStringList("conditions"));
            List<Action> actionList = getActionList(section.getStringList("actions"));
            commandBlockGroupSet.add(
                    new CommandGroup(
                            commandsID,
                            blockType,
                            blockAliases,
                            section.getStringList("commands"),
                            conditionList,
                            actionList
                    )
            );
            if (blockType != BlockType.STRING) {
                break;
            }
            boolean shouldAddToHideList = false;
            for (Action action : actionList) {
                if (action.type() == ActionType.HIDE || action.type() == ActionType.LITE_HIDE) {
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
        this.commandBlockGroupSet = ImmutableSet.copyOf(commandBlockGroupSet);
        this.commandHideGroupSet = ImmutableSet.copyOf(commandHideGroupSet);
    }

    public void setupSymbols(String path) {
        final FileConfiguration symbols = getFile(path, "symbols.yml");
        ObjectSet<SymbolGroup> symbolBlockGroupSet = new ObjectOpenHashSet<>();
        for (String symbolsID : symbols.getConfigurationSection("symbols").getKeys(false)) {
            final ConfigurationSection section = symbols.getConfigurationSection("symbols." + symbolsID);
            BlockType blockType = BlockType.valueOf(section.getString("mode").toUpperCase());
            List<String> blockFactor = getBlockFactorList(section.getString("block_factor", ""));
            List<Condition> conditionList = getConditionList(section.getStringList("conditions"));
            List<Action> actionList = getActionList(section.getStringList("actions"));
            symbolBlockGroupSet.add(
                    new SymbolGroup(
                            symbolsID,
                            blockType,
                            blockFactor,
                            section.getStringList("symbols"),
                            section.getStringList("excluded_commands"),
                            conditionList,
                            actionList
                    )
            );
        }
        this.symbolBlockGroupSet = ImmutableSet.copyOf(symbolBlockGroupSet);
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
        for (String condition : conditionStrings) {
            conditionList.add(Condition.fromString(condition));
        }
        return ImmutableList.copyOf(conditionList);
    }

    private ImmutableList<String> getBlockFactorList(String str) {
        return str.contains(";")
                ? ImmutableList.copyOf(str.trim().split(";"))
                : ImmutableList.of(str.trim());
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

//    public FileConfiguration save(FileConfiguration config, String fileName) {
//        try {
//            config.save(new File(plugin.getDataFolder(), fileName));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return config;
//    }
}
