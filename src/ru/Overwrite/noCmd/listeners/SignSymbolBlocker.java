package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.Listener;

import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;

public class SignSymbolBlocker implements Listener {
	
	final Main plugin;
	private final Config pluginConfig;
	
	public SignSymbolBlocker(Main plugin) {
        this.plugin = plugin;
        pluginConfig = plugin.getPluginConfig();
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSyntax(SignChangeEvent e) {
        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        Player p = e.getPlayer();
        if (!isAdmin(p) && containsBlockedSymbol(line0) || containsBlockedSymbol(line1) || containsBlockedSymbol(line2) || containsBlockedSymbol(line3)) {
            String blockedSymbol = getBlockedSymbol(line0, line1, line2, line3);
            p.sendMessage(pluginConfig.messages_blockedsignsymbol.replace("%symbol%", blockedSymbol));
            e.setCancelled(true);
            if (pluginConfig.settings_enable_sounds) {
                p.playSound(p.getLocation(), pluginConfig.sounds_blocked_command_sound,
                        pluginConfig.sounds_blocked_command_volume, pluginConfig.sounds_blocked_command_pitch);
            }
            if (pluginConfig.settings_enable_titles) {
            	plugin.sendTitleMessage(pluginConfig.titles_blockedsignsymbol.replace("%symbol%", blockedSymbol).split(":"), p);
            }
            if (pluginConfig.settings_notify) {
            	String notifyMessage = pluginConfig.notify_blockedsignsymbol.replace("%player%", p.getName()).replace("%symbol%", blockedSymbol);
            	for (Player admin : Bukkit.getOnlinePlayers()) {
            		if (admin.hasPermission("ublocker.admin")) {
            			admin.sendMessage(notifyMessage);
            			if (pluginConfig.settings_enable_sounds) {
            				admin.playSound(admin.getLocation(), pluginConfig.sounds_admin_notify_sound,
            						pluginConfig.sounds_admin_notify_volume, pluginConfig.sounds_admin_notify_pitch);
                        }
                    }
                }
            }
        }
    }

    private boolean containsBlockedSymbol(String line) {
        for (String symbol : pluginConfig.blockedsignsymbol) {
            if (line.contains(symbol)) {
                return true;
            }
        }
        return false;
    }

    private String getBlockedSymbol(String line0, String line1, String line2, String line3) {
        for (String symbol : pluginConfig.blockedsignsymbol) {
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
        return player.hasPermission("ublocker.bypass.symbol") || pluginConfig.excludedplayers.contains(player.getName());
    }
}
