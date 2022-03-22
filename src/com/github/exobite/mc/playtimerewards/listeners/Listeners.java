package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_UPDATE_AVAILABLE, newVersion, currentVersion));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(e.getMessage().toLowerCase(Locale.ROOT).startsWith("convert")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    e.getPlayer().sendMessage(Utils.formatTimeMsToString(10000, "%d%h%m%s"));
                }
            }.runTask(PluginMaster.getInstance());
        }

    }

}
