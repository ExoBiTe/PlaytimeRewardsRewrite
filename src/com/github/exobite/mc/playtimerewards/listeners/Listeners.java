package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PlayerData;
import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Listeners implements Listener {

    public static PlayerData lastJoined = null;

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        lastJoined = PlayerManager.getInstance().createPlayerData(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        ItemStack is = e.getPlayer().getInventory().getItemInMainHand();
        if(is==null) return;
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) && is.getType()== Material.STICK) {
            e.setCancelled(true);
            PlayerManager.getInstance().getPlayerData(e.getPlayer()).GUI.openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        PlayerManager.getInstance().getPlayerData(e.getPlayer()).onLeave(false);
    }

}
