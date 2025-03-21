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
import ru.overwrite.ublocker.actions.ActionType;
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

    public void executeActions(Cancellable e, Player p, String command, String symbol, List<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, symbol, command};

        for (Action action : actions) {
            ActionType type = action.type();

            if (shouldBlockAction(type, p, action)) {
                e.setCancelled(true);
                continue;
            }

            if (e.isCancelled()) {
                switch (type) {
                    case MESSAGE -> sendMessageAsync(p, action, replacementList);
                    case TITLE -> sendTitleAsync(p, action, replacementList);
                    case ACTIONBAR -> sendActionBarAsync(p, action, replacementList);
                    case SOUND -> sendSoundAsync(p, action);
                    case CONSOLE -> executeConsoleCommand(p, action);
                    case LOG -> logAction(action, replacementList);
                    case NOTIFY -> sendNotifyAsync(p, action, replacementList);
                    case NOTIFY_CONSOLE -> sendNotifyConsoleAsync(action, replacementList);
                    case NOTIFY_SOUND -> sendNotifySoundAsync(action);
                }
            }
        }
    }

    private boolean shouldBlockAction(ActionType type, Player p, Action action) {
        return switch (type) {
            case BLOCK -> true;
            case LITE_BLOCK -> !hasBypassPermission(p, action);
            default -> false;
        };
    }

    private void sendMessageAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            Component component = Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS);
            p.sendMessage(component);
        });
    }

    private void sendTitleAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            String[] titleMessages = formattedMessage.split(";");
            Utils.sendTitleMessage(titleMessages, p);
        });
    }

    private void sendActionBarAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String message = formatActionMessage(action, replacementList);
            p.sendActionBar(message);
        });
    }

    private void sendSoundAsync(Player p, Action action) {
        runner.runAsync(() -> Utils.sendSound(action.context().split(";"), p));
    }

    private void executeConsoleCommand(Player p, Action action) {
        runner.run(() -> Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                action.context().replace("%player%", p.getName())
        ));
    }

    private void logAction(Action action, String[] replacementList) {
        String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER, true);
        String file = Utils.extractValue(action.context(), Utils.FILE_PREFIX, "}");
        plugin.logAction(Utils.replaceEach(logMessage, searchList, replacementList), file);
    }

    private void sendNotifyAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String formattedMessage = formatActionMessage(action, replacementList);
            Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(perm))
                    .forEach(player -> player.sendMessage(component));

            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serialize(component);
                plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
            }
        });
    }

    private void sendNotifyConsoleAsync(Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            Bukkit.getConsoleSender().sendMessage(formattedMessage);
        });
    }

    private void sendNotifySoundAsync(Action action) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER, true).split(";");

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(perm))
                    .forEach(player -> Utils.sendSound(sound, player));
        });
    }

    private String formatActionMessage(Action action, String[] replacementList) {
        return Utils.replaceEach(
                Utils.COLORIZER.colorize(action.context()),
                searchList,
                replacementList
        );
    }

    private boolean hasBypassPermission(Player p, Action action) {
        return p.hasPermission(getActionPermission(action, "ublocker.bypass.symbols"));
    }

    private String getActionPermission(Action action, String defaultPerm) {
        return Utils.getPermOrDefault(
                Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"),
                defaultPerm
        );
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