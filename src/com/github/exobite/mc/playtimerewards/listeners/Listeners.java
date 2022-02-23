package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PlayerData;
import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {

    public static PlayerData lastJoined = null;

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        lastJoined = PlayerManager.getInstance().createPlayerData(e.getPlayer());
        //TODO: Notify Players with the correct Permission that a new Update is available (Also create Message in Lang)
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessageWithArgs("NOTIF_UPDATE_AVAILABLE", newVersion, currentVersion));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        PlayerManager.getInstance().getPlayerData(e.getPlayer()).onLeave(false);
    }

}
