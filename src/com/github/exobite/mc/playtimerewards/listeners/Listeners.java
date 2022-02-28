package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.APIReturnAction;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.MojangAPI;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Locale;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessageWithArgs("NOTIF_UPDATE_AVAILABLE", newVersion, currentVersion));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if(e.getMessage().toLowerCase(Locale.ROOT).startsWith("edit")) {
            e.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Reward rw = RewardManager.getInstance().getRewardFromName("SampleReward1");
                    RewardManager.getInstance().startRewardEdit(rw, e.getPlayer());
                }
            }.runTask(PluginMaster.getInstance());
        }else if(e.getMessage().toLowerCase(Locale.ROOT).startsWith("getname")) {
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
