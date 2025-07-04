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

        BlockType mode = BlockType.valueOf(allowedChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedChars.getString("pattern"));
        }

        List<Action> actionList = getActionList(allowedChars.getStringList("actions"));

        this.chatCharsSettings = new ChatCharsSettings(
                mode,
                charSet,
                pattern,
                actionList
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

        BlockType mode = BlockType.valueOf(allowedBookChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedBookChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedBookChars.getString("pattern"));
        }

        List<Action> actionList = getActionList(allowedBookChars.getStringList("actions"));

        this.bookCharsSettings = new BookCharsSettings(
                mode,
                charSet,
                pattern,
                actionList
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

        BlockType mode = BlockType.valueOf(allowedSignChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedSignChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedSignChars.getString("pattern"));
        }

        List<Action> actionList = getActionList(allowedSignChars.getStringList("actions"));

        this.signCharsSettings = new SignCharsSettings(
                mode,
                charSet,
                pattern,
                actionList
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

        BlockType mode = BlockType.valueOf(allowedCommandChars.getString("mode").toUpperCase());
        CharSet charSet = null;
        Pattern pattern = null;
        switch (mode) {
            case STRING -> charSet = new CharOpenHashSet(allowedCommandChars.getString("pattern").toCharArray());
            case PATTERN -> pattern = Pattern.compile(allowedCommandChars.getString("pattern"));
        }

        List<Action> actionList = getActionList(allowedCommandChars.getStringList("actions"));

        this.commandCharsSettings = new CommandCharsSettings(
                mode,
                charSet,
                pattern,
                actionList
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

        int maxNumbers = numbersCheck.getInt("maxmsgnumbers", 12);
        boolean strictCheck = numbersCheck.getBoolean("strict");

        List<Action> actionList = getActionList(numbersCheck.getStringList("actions"));

        this.numberCheckSettings = new NumberCheckSettings(
                maxNumbers,
                strictCheck,
                actionList
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

        int maxUpperCasePercent = caseCheck.getInt("max_uppercase_percent", 70);
        boolean strictCheck = caseCheck.getBoolean("strict");

        List<Action> actionList = getActionList(caseCheck.getStringList("actions"));

        this.caseCheckSettings = new CaseCheckSettings(
                maxUpperCasePercent,
                strictCheck,
                actionList
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

        int samePercents = sameMessages.getInt("same_percents", 70);
        int maxSameMessage = sameMessages.getInt("max_same_message", 2);
        int minMessageLength = sameMessages.getInt("min_message_length", 3);
        int historySize = sameMessages.getInt("history_size", 10);

        List<Action> actionList = getActionList(sameMessages.getStringList("actions"));

        this.sameMessagesSettings = new SameMessagesSettings(
                samePercents,
                maxSameMessage,
                minMessageLength,
                historySize,
                actionList
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

        boolean strict = banWords.getBoolean("strict");
        String censorSymbol = String.valueOf(banWords.getString("censor_symbol", "*").charAt(0));

        List<Action> actionList = getActionList(banWords.getStringList("actions"));

        this.banWordsSettings = new BanWordsSettings(
                mode,
                banWordsString,
                banWordsPattern,
                strict,
                censorSymbol,
                actionList
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
