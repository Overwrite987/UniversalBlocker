package ru.overwrite.ublocker.listeners.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CaseCheckSettings;
import ru.overwrite.ublocker.utils.Utils;

@Deprecated(forRemoval = true) // Звёздочка об этом позаботится
public class CaseCheck implements Listener {

    private final UniversalBlocker plugin;
    private final Config pluginConfig;

    public CaseCheck(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCaseCheck(AsyncPlayerChatEvent e) {
        CaseCheckSettings caseCheckSettings = pluginConfig.getCaseCheckSettings();
        if (caseCheckSettings == null) return;

        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage();
        int threshold = caseCheckSettings.maxUpperCasePercent();
        if (checkCase(message, threshold)) {
            if (caseCheckSettings.strictCheck()) {
                cancelChatEvent(p, message, e);
                return;
            }
            e.setMessage(message.toLowerCase());
        }
    }

    private boolean checkCase(String message, double threshold) {
        int totalChars = message.length();
        int uppercaseCount = 0;
        for (int i = 0; i < totalChars; i++) {
            if (Character.isUpperCase(message.charAt(i))) {
                uppercaseCount++;
            }
        }
        double percentage = (double) uppercaseCount / totalChars * 100;
        return percentage > threshold;
    }

    private final String[] searchList = {"%player%", "%limit%", "%msg%"};

    private void cancelChatEvent(Player p, String message, Cancellable e) {
        CaseCheckSettings caseCheckSettings = pluginConfig.getCaseCheckSettings();
        e.setCancelled(true);
        p.sendMessage(caseCheckSettings.message().replace("%limit%", Integer.toString(caseCheckSettings.maxUpperCasePercent())));
        if (caseCheckSettings.enableSounds()) {
            Utils.sendSound(caseCheckSettings.sound(), p);
        }
        if (caseCheckSettings.notifyEnabled()) {
            String[] replacementList = {p.getName(), Integer.toString(caseCheckSettings.maxUpperCasePercent()), message};

            String formattedMessage = Utils.replaceEach(caseCheckSettings.notifyMessage(), searchList, replacementList);

            String notifyMessage = Utils.extractMessage(formattedMessage, Utils.NOTIFY_MARKERS);
            String hoverText = Utils.extractValue(formattedMessage, Utils.HOVER_TEXT_PREFIX, "}");
            String clickEvent = Utils.extractValue(formattedMessage, Utils.CLICK_EVENT_PREFIX, "}");

            Component component = LegacyComponentSerializer.legacySection().deserialize(notifyMessage);
            if (hoverText != null) {
                component = Utils.createHoverEvent(component, hoverText);
            }
            if (clickEvent != null) {
                component = Utils.createClickEvent(component, clickEvent);
            }

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(component);
                    if (caseCheckSettings.notifySoundsEnabled()) {
                        Utils.sendSound(caseCheckSettings.notifySound(), admin);
                    }
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.banwords") || plugin.isExcluded(player));
    }
}
