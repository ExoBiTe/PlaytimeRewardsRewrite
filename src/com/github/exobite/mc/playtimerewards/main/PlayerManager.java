package com.github.exobite.mc.playtimerewards.main;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerManager {

    private static PlayerManager instance;

    public static PlayerManager getInstance() {
        if(instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    private Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    private PlayerManager() {}

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
        return registeredPlayers.containsKey(id) ? registeredPlayers.get(id) : null;
    }

}
