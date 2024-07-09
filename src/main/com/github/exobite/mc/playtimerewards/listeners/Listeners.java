package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.AFKManager;
import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.utils.*;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class Listeners implements Listener {

    /*

    This Listener class is only loaded & registered, when AuthMe isnt loaded!
    When AuthMe is loaded, these Listeners are moved to ../external/authme/AuthMeManager.java

    */

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_UPDATE_AVAILABLE, newVersion, currentVersion));
        }
        PlayerManager.getInstance().onJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        if(Config.getInstance().enableAfkSystem()) AFKManager.getInstance().onQuit(e);
        PlayerManager.getInstance().onLeave(e);
    }

}
