package ru.Overwrite.noCmd.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.Listener;
import ru.Overwrite.noCmd.Main;
import ru.Overwrite.noCmd.utils.Config;
import ru.Overwrite.noCmd.utils.RGBcolors;

public class SignSymbolBlocker implements Listener {

    private final Main main = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSyntax(SignChangeEvent e) {
    	FileConfiguration config = main.getConfig();
        FileConfiguration messageConfig = Config.messages;
        String line0 = e.getLine(0);
        String line1 = e.getLine(1);
        String line2 = e.getLine(2);
        String line3 = e.getLine(3);
        Player p = e.getPlayer();
        if (!isAdmin(p) && containsBlockedSymbol(line0) || containsBlockedSymbol(line1) || containsBlockedSymbol(line2) || containsBlockedSymbol(line3)) {
            String blockedSymbol = getBlockedSymbol(line0, line1, line2, line3);
            String symbolMessage = RGBcolors.translate(messageConfig.getString("messages.blockedsignsymbol")).replace("%symbol%", blockedSymbol);
            p.sendMessage(symbolMessage);
            e.setCancelled(true);
            if (config.getBoolean("settings.enable-sounds")) {
                p.playSound(p.getLocation(), Sound.valueOf(config.getString("sounds.blocked-command.sound")),
                        (float) config.getDouble("sounds.blocked-command.volume"), (float) config.getDouble("sounds.blocked-command.pitch"));
            }
            if (config.getBoolean("settings.enable-titles")) {
                String[] titleMessages = messageConfig.getString("messages.blockedsignsymbol-title").split(":");
                String title = RGBcolors.translate(titleMessages[0]);
                String subtitle = RGBcolors.translate(titleMessages[1]).replace("%symbol%", blockedSymbol);
                int fadeIn = Integer.parseInt(titleMessages[2]);
    			int stay = Integer.parseInt(titleMessages[3]);
    			int fadeOut = Integer.parseInt(titleMessages[4]);
    			p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }
            if (config.getBoolean("settings.notify")) {
                String notifyMessage = RGBcolors.translate(messageConfig.getString("messages.notify-signsymbol").replace("%player%", p.getName()).replace("%symbol%", blockedSymbol));
                Bukkit.broadcast(notifyMessage, "ublocker.admin");

                if (config.getBoolean("settings.enable-sounds")) {
                    for (Player admin : Bukkit.getOnlinePlayers()) {
                        if (admin.hasPermission("ublocker.admin")) {
                            admin.playSound(admin.getLocation(), Sound.valueOf(config.getString("sounds.admin-notify.sound")),
                                    (float) config.getDouble("sounds.admin-notify.volume"), (float) config.getDouble("sounds.admin-notify.pitch"));
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
