package ru.overwrite.ublocker.listeners.chat;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import ru.overwrite.ublocker.UniversalBlocker;
import ru.overwrite.ublocker.configuration.Config;
import ru.overwrite.ublocker.configuration.data.CancellationSettings;
import ru.overwrite.ublocker.utils.PluginMessage;
import ru.overwrite.ublocker.utils.Utils;

public abstract class ChatListener implements Listener {

    protected final UniversalBlocker plugin;
    protected final Config pluginConfig;

    @Getter
    @Setter
    protected boolean registered;

    public ChatListener(UniversalBlocker plugin) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getPluginConfig();
    }

    protected void cancelEvent(Player p, String[] searchList, String[] replacementList, Cancellable e, CancellationSettings cancellationSettings, PluginMessage pluginMessage) {
        e.setCancelled(true);
        p.sendMessage(cancellationSettings.message());
        Utils.sendSound(cancellationSettings.sound(), p);
        if (cancellationSettings.notifyEnabled()) {

            String formattedMessage = Utils.replaceEach(cancellationSettings.notifyMessage(), searchList, replacementList);

            Component component = Utils.parseMessage(formattedMessage, Utils.NOTIFY_MARKERS);

            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (admin.hasPermission("ublocker.admin")) {
                    admin.sendMessage(component);
                    Utils.sendSound(cancellationSettings.notifySound(), admin);
                }
            }
            if (pluginMessage != null) {
                String gsonMessage = GsonComponentSerializer.gson().serializer().toJsonTree(component).toString();
                pluginMessage.sendCrossProxyBasic(p, gsonMessage);
            }
        }
    }

    protected boolean isAdmin(Player player, String permission) {
        return player.hasPermission(permission) || plugin.isExcluded(player);
    }
}
