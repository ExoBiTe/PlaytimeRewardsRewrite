package com.github.exobite.mc.playtimerewards.main;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public PlayerData getPlayerData(Player p){
        return registeredPlayers.containsKey(p.getUniqueId()) ? registeredPlayers.get(p.getUniqueId()) : null;
    }

}
