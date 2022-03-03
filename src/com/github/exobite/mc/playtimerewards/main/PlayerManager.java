package com.github.exobite.mc.playtimerewards.main;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class PlayerManager implements Listener {

    private static PlayerManager instance;

    public static PlayerManager getInstance() {
        return instance;
    }

    public static PlayerManager registerPlayerManager(JavaPlugin main) {
        if(instance == null) {
            instance = new PlayerManager(main);
        }
        return instance;
    }

    private final JavaPlugin main;
    private final Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    private PlayerManager(JavaPlugin main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);

        long ticks = Config.getInstance().getAutoSaveTimerMS() / 50;
        if(ticks>0){
            BukkitRunnable rb = new BukkitRunnable() {

                private final File f = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "playerData.yml");

                @Override
                public void run() {
                    long ms1 = System.currentTimeMillis();
                    if(registeredPlayers.size()<=0 || PluginMaster.getInstance().pauseAsyncTimers()) {
                        //No PlayerData registered, so there is nothing to save.
                        return;
                    }
                    PluginMaster.sendConsoleMessage(Level.INFO, "Saving PlayerData from all Online Players...");
                    YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                    int amount = 0;
                    for(PlayerData pd:registeredPlayers.values()) {
                        pd.massSaveData(conf);
                        amount++;
                    }
                    try {
                        conf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    PluginMaster.sendConsoleMessage(Level.INFO, "Saved Data for "+amount+" Players (took "+(System.currentTimeMillis()-ms1)+"ms)!");
                }

            };
            BukkitTask bt = rb.runTaskTimerAsynchronously(this.main, ticks, ticks);
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e){
        createPlayerData(e.getPlayer());
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e){
        getPlayerData(e.getPlayer()).onLeave(false);
        removePlayerData(e.getPlayer().getUniqueId());
    }

    public void refreshRewardData() {
        for(PlayerData pDat:registeredPlayers.values()) {
            pDat.refreshRewards();
        }
    }

    public PlayerData createPlayerData(Player p){
        UUID pID = p.getUniqueId();
        boolean exists = false;
        for(UUID id : registeredPlayers.keySet()) {
            if(pID == id) {
                exists = true;
                break;
            }
        }
        if(!exists){
            PlayerData pDat = new PlayerData(p);
            registeredPlayers.put(pID, pDat);
            return pDat;
        }else{
            return null;
        }
    }

    public void cleanAllPlayerData() {
        File f = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "playerData.yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        for(PlayerData pd:registeredPlayers.values()) {
            pd.massSaveData(conf);
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        registeredPlayers.clear();
    }

    public boolean removePlayerData(UUID id) {
        if(!registeredPlayers.containsKey(id)) return false;
        registeredPlayers.remove(id);
        return true;
    }

    public PlayerData getPlayerData(Player p){
        return getPlayerData(p.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id){
        return registeredPlayers.getOrDefault(id, null);
    }



}
