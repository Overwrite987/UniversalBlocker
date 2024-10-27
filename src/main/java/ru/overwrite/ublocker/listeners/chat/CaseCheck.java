package ru.overwrite.ublocker.listeners.chat;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import ru.overwrite.ublocker.Main;
import ru.overwrite.ublocker.utils.Config;
import ru.overwrite.ublocker.utils.Utils;

@Deprecated(forRemoval = true) // Звёздочка об этом позаботится
public class CaseCheck implements Listener {

    private final Main plugin;
    private final Config pluginConfig;
    public static boolean enabled = false;

    public CaseCheck(Main plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCaseCheck(AsyncPlayerChatEvent e) {
        if (!enabled) return;
        Player p = e.getPlayer();
        if (isAdmin(p))
            return;
        String message = e.getMessage();
        String messageToCheck = message.replace(" ", "");
        int threshold = pluginConfig.maxcuppercasepercent;
        if (checkCase(messageToCheck, threshold)) {
            if (pluginConfig.strict_case_check) {
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
        e.setCancelled(true);
        p.sendMessage(pluginConfig.case_check_message.replace("%limit%", Integer.toString(pluginConfig.maxcuppercasepercent)));
        if (pluginConfig.case_check_enable_sounds) {
            p.playSound(p.getLocation(),
                    Sound.valueOf(pluginConfig.case_check_sound_id),
                    pluginConfig.case_check_sound_volume,
                    pluginConfig.case_check_sound_pitch);
        }
        if (pluginConfig.case_check_notify) {

            String[] replacementList = {p.getName(), Integer.toString(pluginConfig.maxcuppercasepercent), message};

            String notifyMessage = Utils.replaceEach(pluginConfig.case_check_notify_message, searchList, replacementList);

            final Component comp = Utils.createHoverMessage(notifyMessage);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(comp);
                    if (pluginConfig.case_check_notify_sounds) {
                        admin.playSound(admin.getLocation(),
                                Sound.valueOf(pluginConfig.case_check_notify_sound_id),
                                pluginConfig.case_check_notify_sound_volume,
                                pluginConfig.case_check_notify_sound_pitch);
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
