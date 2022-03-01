package com.github.exobite.mc.playtimerewards.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AFKManager implements Listener {

    private static AFKManager instance;

    public static AFKManager getInstance() {
        return instance;
    }

    public static AFKManager register(JavaPlugin main) {
        if(instance==null) {
            instance = new AFKManager(main);
        }
        return instance;
    }

    //Gets later replaced by Config values
    private final boolean cancelOnInteract = true,
            cancelOnMove = true,
            cancelOnChat = true,
            cancelOnLook = true,
            cancelOnCommand = true;

    private final Map<UUID, Long> afkCounters = new HashMap<>();
    private final Map<UUID, Long> isAfk = new HashMap<>();
    private final JavaPlugin main;

    private final long FLAGGED_AS_AFK_SECONDS = 30 * 1;  //5 Minutes

    private AFKManager(JavaPlugin main){
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
        runScheduler();
    }

    /*
    Notes:
    What happens when a afk user gets targeted by /pt?
        -> Is it gonna subtract the AFK-Time until then before delivering the result?
        -> Or am i gonna ignore this fact and just correct the playtime when the user comes back
    How to keep up with the afk-time?
        -> Bukkit only allows to decrement a statistic by 2^32/2 -> ~ 30.000h -> 1.242d -> 3,4years
        -> Nah... i guess this is enough time
    BB

     */

    private void runScheduler(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for(UUID id:afkCounters.keySet()) {
                    long newval = afkCounters.get(id) + 1;
                    if(newval>= FLAGGED_AS_AFK_SECONDS) {
                        afkCounters.remove(id);
                        goAfk(id);
                    }else{
                        afkCounters.put(id, newval);
                    }
                }
            }
        }.runTaskTimerAsynchronously(main, 20L, 20L);
    }

    private void goAfk(UUID id) {
        isAfk.put(id, System.currentTimeMillis() - (FLAGGED_AS_AFK_SECONDS * 1000));
        Objects.requireNonNull(Bukkit.getPlayer(id)).sendMessage("Youre now flagged as afk.");
    }

    private void comeBack(Player p) {
        UUID id = p.getUniqueId();
        long diff = System.currentTimeMillis() - isAfk.get(id);
        isAfk.remove(id);
        p.sendMessage("Wb, "+diff+" ms");
        long diffInTicks = diff / 50;
        if(diffInTicks>Integer.MAX_VALUE) {
            //This shouldn't happen, as it is a very loooong time
            //Anyways, workaround:
            int multiplied = Math.toIntExact(diffInTicks / Integer.MAX_VALUE);
            int rest = Math.toIntExact(diffInTicks % Integer.MAX_VALUE);
            for(int i=0;i<multiplied;i++) {
                decreasePlaytime(p, Integer.MAX_VALUE);
            }
            decreasePlaytime(p, rest);
        }else{
            decreasePlaytime(p, Math.toIntExact(diffInTicks));
        }
    }

    private void decreasePlaytime(Player p, int ticks) {
        int newticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE) - ticks;
        if(newticks<0) newticks = 0;
        p.sendMessage("Setting playedoneminute to "+newticks);
        p.sendMessage("It is now "+p.getStatistic(Statistic.PLAY_ONE_MINUTE));
        p.setStatistic(Statistic.PLAY_ONE_MINUTE, newticks);
        p.sendMessage("Now it is "+p.getStatistic(Statistic.PLAY_ONE_MINUTE));
    }

    private void resetAfk(Player p){
        UUID id = p.getUniqueId();
        if(isAfk.containsKey(id)) {
            comeBack(p);
        }
        afkCounters.put(id, 0L);
    }


    @EventHandler(priority = EventPriority.HIGH)
    private void onMove(PlayerMoveEvent e){
        boolean moved = moved(e.getFrom(), e.getTo());
        if(!moved && cancelOnLook || moved && cancelOnMove) {
            resetAfk(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onInteract(PlayerInteractEvent e) {
        if(cancelOnInteract) {
            resetAfk(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onChat(AsyncPlayerChatEvent e) {
        if(cancelOnChat) {
            resetAfk(e.getPlayer());
        }
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent e){
        if(cancelOnCommand) {
            resetAfk(e.getPlayer());
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        afkCounters.put(e.getPlayer().getUniqueId(), 0L);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        if(isAfk.containsKey(id)) {
            comeBack(e.getPlayer());
        }else{
            afkCounters.remove(id);
        }
    }

    private boolean moved(Location from, Location to) {
        if(to==null) return false;  //When does null appear at this Parameter?
        return (from.getX()!=to.getX() || from.getY()!=to.getY() || from.getZ()!=to.getZ());
    }

}
