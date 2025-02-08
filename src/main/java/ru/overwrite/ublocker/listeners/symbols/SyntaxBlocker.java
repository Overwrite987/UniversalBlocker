package ru.overwrite.ublocker.listeners.symbols;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.blockgroups.SymbolGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public SyntaxBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        String command = e.getMessage().toLowerCase();
        for (SymbolGroup group : pluginConfig.getSymbolBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.getGroupId());
            if (group.getBlockFactor().isEmpty() || !group.getBlockFactor().contains("command")) {
                Utils.printDebug("Group " + group.getGroupId() + " does not have 'command' block factor. Skipping...");
                continue;
            }
            List<Action> actions = group.getActionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.getConditionsToCheck())) {
                Utils.printDebug("Blocking does not fulfill the requirements. Skipping group...");
                continue;
            }
            switch (group.getBlockType()) {
                case STRING: {
                    checkStringBlock(e, p, command, group);
                    break;
                }
                case PATTERN: {
                    checkPatternBlock(e, p, command, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(PlayerCommandPreprocessEvent e, Player p, String command, SymbolGroup group) {
        for (String symbol : group.getSymbolsToBlock()) {
            List<Action> actions = group.getActionsToExecute();
            if (startWithExcludedString(command, group.getExcludedCommandsString())) {
                continue;
            }
            if (command.contains(symbol)) {
                executeActions(e, p, command, symbol, actions, p.getWorld().getName());
            }
        }
    }

    private void checkPatternBlock(PlayerCommandPreprocessEvent e, Player p, String command, SymbolGroup group) {
        for (Pattern pattern : group.getPatternsToBlock()) {
            Matcher matcher = pattern.matcher(command);
            if (startWithExcludedPattern(command, group.getExcludedCommandsPattern())) {
                continue;
            }
            if (matcher.find()) {
                List<Action> actions = group.getActionsToExecute();
                executeActions(e, p, command, matcher.group(), actions, p.getWorld().getName());
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%symbol%", "%msg%"};

    private void executeActions(Cancellable e, Player p, String command, String symbol, List<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, symbol, command};
        for (Action action : actions) {
            switch (action.type()) {
                case BLOCK: {
                    e.setCancelled(true);
                    break;
                }
                case LITE_BLOCK: {
                    String perm = Utils.getPermOrDefault(
                            Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                            "ublocker.bypass.symbols");
                    if (!p.hasPermission(perm)) {
                        e.setCancelled(true);
                    }
                    break;
                }
                case MESSAGE: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Component component = Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS);
                        p.sendMessage(component);
                    });
                    break;
                }
                case TITLE: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String[] titleMessages = Utils.replaceEach(coAction, searchList, replacementList).split(";");
                        Utils.sendTitleMessage(titleMessages, p);
                    });
                    break;
                }
                case ACTIONBAR: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String coAction = Utils.COLORIZER.colorize(action.context());
                        String message = Utils.replaceEach(coAction, searchList, replacementList);
                        p.sendActionBar(message);
                    });
                    break;
                }
                case SOUND: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String[] sound = action.context().split(";");
                        Utils.sendSound(sound, p);
                    });
                    break;
                }
                case CONSOLE: {
                    if (!e.isCancelled())
                        return;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
                    break;
                }
                case LOG: {
                    if (!e.isCancelled())
                        return;
                    String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER, true);
                    String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
                    plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
                    break;
                }
                case NOTIFY: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");

                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

                        Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission(perm))
                                .forEach(player -> player.sendMessage(component));

                        if (plugin.getPluginMessage() != null) {
                            String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                            plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
                        }
                    });
                    break;
                }
                case NOTIFY_CONSOLE: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String formattedMessage = Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
                        Bukkit.getConsoleSender().sendMessage(formattedMessage);
                    });
                    break;
                }
                case NOTIFY_SOUND: {
                    if (!e.isCancelled())
                        return;
                    runner.runAsync(() -> {
                        String perm = Utils.getPermOrDefault(
                                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                                "ublocker.admin");
                        String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER, true).split(";");
                        Bukkit.getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission(perm))
                                .forEach(player -> Utils.sendSound(sound, player));
                    });
                    break;
                }
                default:
                    break;
            }
        }
    }

    private boolean startWithExcludedString(String command, List<String> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (String excluded : excludedList) {
            if (command.startsWith(excluded + " ")) {
                return true;
            }
        }
        return false;
    }

    private boolean startWithExcludedPattern(String command, List<Pattern> excludedList) {
        if (excludedList.isEmpty()) {
            return false;
        }
        for (Pattern excluded : excludedList) {
            Matcher matcher = excluded.matcher(command);
            if (matcher.lookingAt()) {
                return true;
            }
        }
        return false;
    }
}