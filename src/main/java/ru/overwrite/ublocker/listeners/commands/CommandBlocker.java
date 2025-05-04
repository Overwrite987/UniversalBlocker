package ru.overwrite.ublocker.listeners.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.actions.Action;
import ru.overwrite.ublocker.actions.ActionType;
import ru.overwrite.ublocker.blockgroups.CommandGroup;
import ru.overwrite.ublocker.conditions.ConditionChecker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.task.Runner;
import ru.overwrite.ublocker.utils.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandBlocker implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final Runner runner;

    public static boolean FULL_LOCK;

    public CommandBlocker(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
        this.runner = plugin.getRunner();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (FULL_LOCK) {
            e.setCancelled(true);
            return;
        }
        Player p = e.getPlayer();
        if (plugin.isExcluded(p))
            return;
        String command = e.getMessage().toLowerCase();
        // Дерьмо для фикса другого дерьма
        if (command.length() >= 2 && command.charAt(1) == ' ') {
            e.setCancelled(true);
            Utils.printDebug("Player " + p.getName() + " tried to execute incorrect command: " + command, Utils.DEBUG_COMMANDS);
        }
        for (CommandGroup group : pluginConfig.getCommandBlockGroupSet()) {
            Utils.printDebug("Group checking now: " + group.groupId(), Utils.DEBUG_COMMANDS);
            Utils.printDebug("Block type: " + group.blockType(), Utils.DEBUG_COMMANDS);
            List<Action> actions = group.actionsToExecute();
            if (actions.isEmpty()) {
                continue;
            }
            if (!ConditionChecker.isMeetsRequirements(p, group.conditionsToCheck())) {
                Utils.printDebug("Blocking does not fulfill the requirements. Skipping group...", Utils.DEBUG_COMMANDS);
                continue;
            }
            switch (group.blockType()) {
                case STRING: {
                    checkStringBlock(e, p, command, group);
                    break;
                }
                case PATTERN: {
                    checkPatternGroup(e, p, command, group);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void checkStringBlock(PlayerCommandPreprocessEvent e, Player p, String command, CommandGroup group) {
        for (String com : group.commandsToBlockString()) {
            Command comInMap = Bukkit.getCommandMap().getCommand(com.replace("/", ""));
            List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
            if (!aliases.isEmpty() && !aliases.contains(comInMap.getName())) {
                aliases.add(comInMap.getName());
            }
            String executedCommandBase = Utils.cutCommand(command);
            Utils.printDebug("Executed command base: " + executedCommandBase, Utils.DEBUG_COMMANDS);
            if (executedCommandBase.equalsIgnoreCase(com) || aliases.contains(executedCommandBase.substring(1))) {
                List<Action> actions = group.actionsToExecute();
                if (executeActions(e, p, com, command, actions, p.getWorld().getName())) {
                    break;
                }
            }
        }
    }

    private void checkPatternGroup(PlayerCommandPreprocessEvent e, Player p, String command, CommandGroup group) {
        for (Pattern pattern : group.commandsToBlockPattern()) {
            String executedCommandBase = Utils.cutCommand(command);
            Utils.printDebug("Executed command base: " + executedCommandBase, Utils.DEBUG_COMMANDS);
            Matcher matcher = pattern.matcher(executedCommandBase.replace("/", ""));
            if (matcher.matches()) {
                Command comInMap = Bukkit.getCommandMap().getCommand(matcher.group());
                List<String> aliases = comInMap == null ? List.of() : comInMap.getAliases();
                if (!aliases.isEmpty()) {
                    aliases.add(comInMap.getName());
                }
                List<Action> actions = group.actionsToExecute();
                if (aliases.contains(matcher.group())) {
                    if (executeActions(e, p, matcher.group(), command, actions, p.getWorld().getName())) {
                        break;
                    }
                }
                if (executeActions(e, p, matcher.group(), command, actions, p.getWorld().getName())) {
                    break;
                }
            }
        }
    }

    private final String[] searchList = {"%player%", "%world%", "%cmd%", "%fullcmd%"};

    public boolean executeActions(Cancellable e, Player p, String com, String command, List<Action> actions, String world) {
        final String[] replacementList = {p.getName(), world, com, command};

        for (Action action : actions) {
            ActionType type = action.type();

            if (shouldBlockAction(type, p, action, command)) {
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
        return e.isCancelled();
    }

    private boolean shouldBlockAction(ActionType type, Player p, Action action, String command) {
        return switch (type) {
            case BLOCK -> true;
            case LITE_BLOCK -> !hasBypassPermission(p, action);
            case BLOCK_ARGUMENTS -> hasArguments(command);
            case LITE_BLOCK_ARGUMENTS -> hasArguments(command) && !hasBypassPermission(p, action);
            default -> false;
        };
    }

    private void sendMessageAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String formattedMessage = formatActionMessage(action, replacementList);
            p.sendMessage(Utils.parseMessage(formattedMessage, Utils.HOVER_MARKERS));
        });
    }

    private void sendTitleAsync(Player p, Action action, String[] replacementList) {
        runner.runAsync(() -> {
            String[] titleMessages = formatActionMessage(action, replacementList).split(";");
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.context().replace("%player%", p.getName()));
    }

    private void logAction(Action action, String[] replacementList) {
        String logMessage = Utils.extractMessage(action.context(), Utils.FILE_MARKER);
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
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                plugin.getPluginMessage().sendCrossProxyPerm(p, perm + " " + gsonMessage);
            }
        });
    }

    private void sendNotifyConsoleAsync(Action action, String[] replacementList) {
        runner.runAsync(() -> Bukkit.getConsoleSender().sendMessage(formatActionMessage(action, replacementList)));
    }

    private void sendNotifySoundAsync(Action action) {
        runner.runAsync(() -> {
            String perm = getActionPermission(action, "ublocker.admin");
            String[] sound = Utils.extractMessage(action.context(), Utils.PERM_MARKER).split(";");

            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission(perm))
                    .forEach(player -> Utils.sendSound(sound, player));
        });
    }

    private String formatActionMessage(Action action, String[] replacementList) {
        return Utils.replaceEach(Utils.COLORIZER.colorize(action.context()), searchList, replacementList);
    }

    private boolean hasBypassPermission(Player p, Action action) {
        return p.hasPermission(getActionPermission(action, "ublocker.bypass.commands"));
    }

    private String getActionPermission(Action action, String defaultPerm) {
        return Utils.getPermOrDefault(Utils.extractValue(action.context(), Utils.PERM_PREFIX, "}"), defaultPerm);
    }

    private boolean hasArguments(String command) {
        return command.split(" ").length > 1;
    }
}