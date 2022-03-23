package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.utils.*;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_UPDATE_AVAILABLE, newVersion, currentVersion));
        }
    }

}
