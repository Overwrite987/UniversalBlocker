package ru.overwrite.ublocker.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.ublocker.UniversalBlocker;

public final class PluginMessage implements PluginMessageListener {

    private final UniversalBlocker plugin;

    public PluginMessage(UniversalBlocker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord"))
            return;
        ByteArrayDataInput input = ByteStreams.newDataInput(message);
        String subchannel = input.readUTF();
        if (subchannel.equalsIgnoreCase("ublocker_2")) {
            String data = input.readUTF();
            int index = data.indexOf(' ');
            String perm = data.substring(0, index).trim();
            String notifyMessage = data.substring(index + 1).trim();
            Component comp = GsonComponentSerializer.gson().serializer().fromJson(notifyMessage, Component.class);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission(perm)) {
                    p.sendMessage(comp);
                }
            }
        }
    }

    public void sendCrossProxyPerm(Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("ublocker_2");
        out.writeUTF(message);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

}
