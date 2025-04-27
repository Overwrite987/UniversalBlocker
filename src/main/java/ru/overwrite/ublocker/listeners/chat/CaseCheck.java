package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CaseCheckSettings;

public class CaseCheck implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;
    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    public CaseCheck(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCaseCheck(AsyncPlayerChatEvent e) {
        CaseCheckSettings caseCheckSettings = pluginConfig.getCaseCheckSettings();
        if (caseCheckSettings == null) return;

        Player p = e.getPlayer();
        if (plugin.isAdmin(p, "ublocker.bypass.case")) {
            return;
        }
        String message = e.getMessage();
        int threshold = caseCheckSettings.maxUpperCasePercent();
        if (checkCase(message, threshold)) {
            if (caseCheckSettings.strictCheck()) {
                String[] replacementList = {p.getName(), Integer.toString(caseCheckSettings.maxUpperCasePercent()), message};
                BlockingUtils.cancelEvent(p, searchList, replacementList, e, caseCheckSettings.cancellationSettings(), plugin.getPluginMessage());
                return;
            }
            e.setMessage(message.toLowerCase());
        }
    }

    private boolean checkCase(String message, double threshold) {
        char[] chars = message.toCharArray();
        int uppercaseCount = 0;
        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                uppercaseCount++;
            }
        }
        double percentage = (double) uppercaseCount / chars.length * 100;
        return percentage > threshold;
    }
}
