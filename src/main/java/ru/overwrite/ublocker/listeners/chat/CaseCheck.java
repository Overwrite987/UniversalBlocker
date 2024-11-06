package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.utils.Utils;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CaseCheckSettings;

@Deprecated(forRemoval = true) // Звёздочка об этом позаботится
public class CaseCheck implements Listener {

    private final Main plugin;
    private final Config pluginConfig;

    public CaseCheck(Main plugin) {
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

            String notifyMessage = Utils.replaceEach(caseCheckSettings.notifyMessage(), searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (caseCheckSettings.notifySoundsEnabled()) {
                        Utils.sendSound(caseCheckSettings.notifySound(), admin);
                    }
                }
            }
            if (plugin.getPluginMessage() != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(comp).toString();
                plugin.getPluginMessage().sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    private boolean isAdmin(Player player) {
        return (player.hasPermission("ublocker.bypass.banwords") || plugin.isExcluded(player));
    }
}
