package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.rewards.RewardType;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import jdk.jshell.execution.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    //Object
    private long loginTimestamp;
    private UUID id;
    private boolean hasData;

    private Map<String, GUIManager.GUI> Guis = new HashMap<>();
    private Map<String, Long> receivedTimestamps = new HashMap<>();

    public GUIManager.GUI GUI;
    private boolean allowNextGUIClose = false;

    PlayerData(Player p) {
        id = p.getUniqueId();
        loginTimestamp = Utils.getPlaytimeInMS(p);
        loadPlayerData();


        GUI = GUIManager.createGUI("I´m a cool GUI", 18);
        GUI.canClose(false);
        GUI.setItemstack(new ItemStack(Material.OAK_LOG), 1);
        GUI.setSlotAction(1, new GUIManager.GUIClickAction() {

            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI g) {
                if(!(e.getWhoClicked() instanceof Player)) return;
                Player p = (Player) e.getWhoClicked();
                allowNextGUIClose = true;
                p.closeInventory();
            }
        });

    }

    private void loadPlayerData(){
        hasData = false;
        new BukkitRunnable() {

            @Override
            public void run() {
                //Fill rewardsList with all registered Rewards
                RewardManager.getInstance().getRegisteredRewardNames().forEach(str -> {
                    receivedTimestamps.put(str, 0L);
                });
                File f = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "playerData.yml");
                if(f.exists()) {
                    YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                    if(!conf.getKeys(false).contains(id)) {
                        ConfigurationSection playerSection = conf.getConfigurationSection(id.toString());
                        if(playerSection!=null) {
                            for(String key:playerSection.getKeys(false)){
                                //Check if key is a registered reward
                                if(receivedTimestamps.containsKey(key)) receivedTimestamps.put(key, playerSection.getLong(key, 0L));
                            }
                        }
                    }
                }
                hasData = true;
            }
        }.runTaskAsynchronously(PluginMaster.getInstance());
    }

    private void savePlayerData(boolean sync) {
        BukkitRunnable rb = new BukkitRunnable() {

            @Override
            public void run() {
                File f = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "playerData.yml");
                YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                String px = id.toString() + ".";
                receivedTimestamps.keySet().forEach(str -> {
                    long val = receivedTimestamps.get(str);
                    System.out.println("Value for" + str + " is " + val);
                    if(val!=0){
                        conf.set(px + str, val);
                    }
                });
                try {
                    conf.save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        if(sync) {
            rb.runTask(PluginMaster.getInstance());
        }else{
            rb.runTaskAsynchronously(PluginMaster.getInstance());
        }
    }

    protected void massSaveData(YamlConfiguration conf){
        String px = id.toString() + ".";
        receivedTimestamps.keySet().forEach(str -> {
            long val = receivedTimestamps.get(str);
            if(val!=0) conf.set(px + str, val);
        });
    }

    public boolean checkReward(Reward rw){
        if(!receivedTimestamps.containsKey(rw.getName())) return false;
        long oldTimestamp = receivedTimestamps.get(rw.getName());
        long nowTimestamp = 0;
        RewardType type = rw.getType();
        boolean grant = false;
        switch (type) {
            case PLAYTIME -> nowTimestamp = Utils.getPlaytimeInMS(p());
            case SESSION_TIME -> nowTimestamp = getSessionTime();
            case GLOBAL_TIME -> nowTimestamp = System.currentTimeMillis();
        }
        grant = nowTimestamp >= oldTimestamp + rw.getTimeMs();
        if(grant) {
            receivedTimestamps.put(rw.getName(), nowTimestamp);
        }
        return grant;
    }

    public boolean hasData() {
        return hasData;
    }

    public void onLeave(boolean saveDataSync) {
        savePlayerData(saveDataSync);
        PlayerManager.getInstance().removePlayerData(id);
    }

    public long getSessionTime() {
        return Utils.getPlaytimeInMS(p()) - loginTimestamp;
    }

    public void allowNextGUIClose(boolean allow){
        allowNextGUIClose = allow;
    }

    public boolean isAllowedToCloseNExtGUI(){
        return allowNextGUIClose;
    }

    public Player p() {
        return Bukkit.getPlayer(id);
    }

}
