package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.APIReturnAction;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.MojangAPI;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessageWithArgs("NOTIF_UPDATE_AVAILABLE", newVersion, currentVersion));
        }
    }

    @EventHandler
    public void onChat(@NotNull AsyncPlayerChatEvent e){
        if(e.getMessage().toLowerCase(Locale.ROOT).startsWith("getname")) {
            e.setCancelled(true);
            MojangAPI.getInstance().getNameFromUUID(e.getPlayer().getUniqueId(), new APIReturnAction() {
                @Override
                public void onFinish(String data) {
                    e.getPlayer().sendMessage("Retrieved "+data+".");
                }
            });
        }else if(e.getMessage().toLowerCase(Locale.ROOT).startsWith("getid")) {
            e.setCancelled(true);
            MojangAPI.getInstance().getUUIDFromName(e.getPlayer().getName(), new APIReturnAction() {
                @Override
                public void onFinish(String data) {
                    e.getPlayer().sendMessage("Retrieved "+data+".");
                }
            });
        }
    }

}
