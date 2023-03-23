package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.Listener;
import ru.Overwrite.noCmd.utils.Config;

public class SignSymbolBlocker implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSyntax(SignChangeEvent e) {
        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        Player p = e.getPlayer();
        if (!isAdmin(p) && containsBlockedSymbol(line0) || containsBlockedSymbol(line1) || containsBlockedSymbol(line2) || containsBlockedSymbol(line3)) {
            String blockedSymbol = getBlockedSymbol(line0, line1, line2, line3);
            p.sendMessage(Config.messages_blockedsignsymbol.replace("%symbol%", blockedSymbol));
            e.setCancelled(true);
            if (Config.settings_enable_sounds) {
                p.playSound(p.getLocation(), Config.sounds_blocked_command_sound,
                        Config.sounds_blocked_command_volume, Config.sounds_blocked_command_pitch);
            }
            if (Config.settings_enable_titles) {
                String[] titleMessages = Config.titles_blockedsignsymbol.split(":");
                String title = titleMessages[0];
                String subtitle = titleMessages[1].replace("%symbol%", blockedSymbol);
                int fadeIn = Integer.parseInt(titleMessages[2]);
    			int stay = Integer.parseInt(titleMessages[3]);
    			int fadeOut = Integer.parseInt(titleMessages[4]);
    			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
            if (Config.settings_notify) {
            	String notifyMessage = Config.notify_blockedsignsymbol.replace("%player%", p.getName()).replace("%symbol%", blockedSymbol);
            	for (Player admin : Bukkit.getOnlinePlayers()) {
            		if (admin.hasPermission("ublocker.admin")) {
            			admin.sendMessage(notifyMessage);
            			if (Config.settings_enable_sounds) {
            				admin.playSound(admin.getLocation(), Config.sounds_admin_notify_sound,
            						Config.sounds_admin_notify_volume, Config.sounds_admin_notify_pitch);
                        }
                    }
                }
            }
        }
    }

    private boolean containsBlockedSymbol(String line) {
        for (String symbol : Config.blockedsignsymbol) {
            if (line.contains(symbol)) {
                return true;
            }
        }
        return false;
    }

    private String getBlockedSymbol(String line0, String line1, String line2, String line3) {
        for (String symbol : Config.blockedsignsymbol) {
            if (line0.contains(symbol)) {
                return symbol;
            } else if (line1.contains(symbol)) {
                return symbol;
            } else if (line2.contains(symbol)) {
                return symbol;
            } else if (line3.contains(symbol)) {
                return symbol;
            }
        }
        return null;
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("ublocker.bypass.symbol") || Config.excludedplayers.contains(player.getName());
    }
}
