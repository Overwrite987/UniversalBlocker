package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.data.CaseCheckSettings;
import ru.overwrite.ublocker.utils.Utils;

public class CaseCheck extends ChatListener {

    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    public CaseCheck(UniversalBlocker plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCaseCheck(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (super.isAdmin(p, "ublocker.bypass.case")) {
            return;
        }
        CaseCheckSettings caseCheckSettings = pluginConfig.getCaseCheckSettings();
        String message = e.getMessage();
        int threshold = caseCheckSettings.maxUpperCasePercent();
        if (checkCase(message, threshold)) {
            if (caseCheckSettings.strictCheck()) {
                String[] replacementList = {p.getName(), Integer.toString(caseCheckSettings.maxUpperCasePercent()), message};
                super.executeActions(e, p, searchList, replacementList, caseCheckSettings.actionsToExecute());
                return;
            }
            Utils.printDebug("Moving message to lower case: " + message, Utils.DEBUG_CHAT);
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
