package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private boolean cancelOnInteract = true;
    private boolean cancelOnMove = true;
    private boolean cancelOnChat = true;
    private boolean cancelOnLook = true;
    private boolean cancelOnCommand = true;

    //Need Thread-Safe Maps here
    private final Map<UUID, Long> afkCounters = new ConcurrentHashMap<>();
    private final Map<UUID, Long> isAfk = new ConcurrentHashMap<>();
    private final JavaPlugin main;

    private long flaggedAsAfkSeconds;
    private boolean isActive;
    private int taskid;

    private AFKManager(JavaPlugin main){
        this.main = main;
        reloadConfig();
    }

    protected void setActive(boolean active) {
        if(isActive == active) return;
        if(active) {
            Bukkit.getServer().getPluginManager().registerEvents(this, main);
            for(Player p:Bukkit.getOnlinePlayers()) {
                if(p.hasPermission("playtimerewards.afk.ignore")) continue;
                afkCounters.put(p.getUniqueId(), 0L);
            }
            runScheduler();
        }else{
            HandlerList.unregisterAll(this);
            for(Player p:Bukkit.getOnlinePlayers()) {
                if(isAfk.containsKey(p.getUniqueId())) {
                    comeBack(p);
                }
                isAfk.clear();
                afkCounters.clear();
            }
            if(taskid!=0) Bukkit.getScheduler().cancelTask(taskid);
            taskid = 0;
        }
        isActive = active;
    }

    protected void reloadConfig(){
        cancelOnInteract = Config.getInstance().isCancelAfkOnInteract();
        cancelOnMove = Config.getInstance().isCancelAfkOnMove();
        cancelOnChat = Config.getInstance().isCancelAfkOnChat();
        cancelOnLook = Config.getInstance().isCancelAfkOnLook();
        cancelOnCommand = Config.getInstance().isCancelAfkOnCommand();
        flaggedAsAfkSeconds = Config.getInstance().getAfkTime();
        setActive(Config.getInstance().enableAfkSystem());
    }

    private void runScheduler(){
        BukkitTask bt = new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Long>> it = afkCounters.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<UUID, Long> entry = it.next();
                    long nVal = entry.getValue()+1;
                    if(nVal >= flaggedAsAfkSeconds) {
                        it.remove();
                        goAfk(entry.getKey());
                    }else{
                        entry.setValue(nVal);
                    }
                }
            }
        }.runTaskTimerAsynchronously(main, 20L, 20L);
        taskid = bt.getTaskId();
    }

    private void goAfk(UUID id) {
        long now = System.currentTimeMillis();
        isAfk.put(id, now - (flaggedAsAfkSeconds * 1000));
        PlayerManager.getInstance().getPlayerData(id).setAfk(true, now);
        Objects.requireNonNull(Bukkit.getPlayer(id)).sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_AFK_USER_WENT_AFK));
    }

    private void comeBack(@NotNull Player p) {
        UUID id = p.getUniqueId();
        long diff = System.currentTimeMillis() - isAfk.get(id);
        isAfk.remove(id);
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
        PlayerManager.getInstance().getPlayerData(id).setAfk(false, 0L);
        p.sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_AFK_USER_CAME_BACK));
    }

    private void decreasePlaytime(@NotNull Player p, int ticks) {
        int newticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE) - ticks;
        if(newticks<0) newticks = 0;
        p.setStatistic(Statistic.PLAY_ONE_MINUTE, newticks);
    }

    private void resetAfk(@NotNull Player p){
        UUID id = p.getUniqueId();
        if(isAfk.containsKey(id)) {
            comeBack(p);
        }
        afkCounters.put(id, 0L);
    }


    @EventHandler(priority = EventPriority.HIGH)
    private void onMove(@NotNull PlayerMoveEvent e){
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
    private void onJoin(@NotNull PlayerJoinEvent e) {
        if(e.getPlayer().hasPermission("playtimerewards.afk.ignore")) return;
        afkCounters.put(e.getPlayer().getUniqueId(), 0L);
    }

    //Moved to listeners.Listeners, due to an Error
    //where the PlayerData gets deleted before this Listener was called.
    public void onQuit(@NotNull PlayerQuitEvent e) {
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
