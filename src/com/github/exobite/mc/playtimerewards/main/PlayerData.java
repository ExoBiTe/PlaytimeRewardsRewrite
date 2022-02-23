package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardData;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.rewards.RewardType;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerData {

    private final long loginTimestamp;
    private final UUID id;
    private boolean hasData;

    private final Map<RewardData, Long> receivedTimestamps = new HashMap<>();

    private boolean allowNextGUIClose = false;

    PlayerData(Player p) {
        id = p.getUniqueId();
        loginTimestamp = Utils.getPlaytimeInMS(p);
        loadPlayerData();

        //Removed for Release Version
        /*GUI = GUIManager.createGUI("I´m a cool GUI", 18);
        GUI.canClose(false);
        GUI.setItemstack(new ItemStack(Material.OAK_LOG), 1);
        GUI.setSlotAction(1, new GUIManager.GUIClickAction() {

            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI g) {
                if(!(e.getWhoClicked() instanceof Player p)) return;
                allowNextGUIClose = true;
                p.closeInventory();
            }
        });*/

    }

    private void loadPlayerData(){
        hasData = false;
        new BukkitRunnable() {

            @Override
            public void run() {
                //Fill rewardsList with all registered Rewards
                RewardManager.getInstance().getRegisteredRewardData().forEach(rwd -> receivedTimestamps.put(rwd, 0L));
                File f = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "playerData.yml");
                if(f.exists()) {
                    YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                    if(conf.getKeys(false).contains(id.toString())) {
                        ConfigurationSection playerSection = conf.getConfigurationSection(id.toString());
                        if(playerSection!=null) {
                            for(String key:playerSection.getKeys(false)){
                                boolean contains = false;
                                for(RewardData rwd: receivedTimestamps.keySet()){
                                    if(rwd.rewardName().equals(key)) {
                                        receivedTimestamps.put(rwd, playerSection.getLong(key, 0L));
                                        contains = true;
                                        break;
                                    }
                                }
                                if(!contains){
                                    //Found a non-existing reward (Maybe a deleted one, or someone messed with the storage file),
                                    //removing it from the Map -> also removing it from the file upon logout
                                    receivedTimestamps.put(new RewardData(key, null, false, false), null);
                                }
                            }
                        }
                    }
                }
                //Set Default Values for "new" Rewards
                for(RewardData rwd:receivedTimestamps.keySet()) {
                    //Faulty Rewards that may not exist (anymore) get stored with a value of null
                    //That's why we get it as Object first, check it against null and then cast it to a long
                    //Getting a Long value directly from the Map causes an NPE.
                    Object valObj = receivedTimestamps.get(rwd);
                    if(valObj==null){
                        continue;
                    }
                    long val = (long) valObj;
                    if(val==0 && rwd.type()==RewardType.PLAYTIME && !rwd.grantFirst()) {
                        long newVal = Utils.getPlaytimeInMS(p());
                        //New Players with no Playtime get offset by 100ms. This is used to save the first timestamp instead of saving 0ms
                        //Saving them with 0ms would effectively prevent the Earning of the Reward until they once reach the wanted Playtime
                        //in one Session, resulting them to save a new timestamp.
                        if(newVal==0) newVal = 100;
                        receivedTimestamps.put(rwd, newVal);
                    }else if(val==0 && rwd.type()==RewardType.GLOBAL_TIME && !rwd.grantFirst()) {
                        receivedTimestamps.put(rwd, loginTimestamp);
                    }else if(val==0 && rwd.type()==RewardType.SESSION_TIME && rwd.grantFirst()) {
                        //Should start SessionRewards with a negative Value, granting it the Player instantly once upon login
                        long timeVal = RewardManager.getInstance().getRewardFromName(rwd.rewardName()).getTimeMs();
                        receivedTimestamps.put(rwd, -1*timeVal);
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
                //Remove SessionData before Saving, not needed
                resetSessionRewardsBeforeSave();
                String px = id + ".";
                receivedTimestamps.keySet().forEach(rwd -> conf.set(px + rwd.rewardName(), receivedTimestamps.get(rwd)));
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
        //Remove SessionData before Saving, not needed
        resetSessionRewardsBeforeSave();
        String px = id.toString() + ".";
        receivedTimestamps.keySet().forEach(rwd -> conf.set(px + rwd.rewardName(), receivedTimestamps.get(rwd)));
    }

    private RewardData getRewardDataFromName(String rewardName) {
        for(RewardData rwd:receivedTimestamps.keySet()) {
            if(rwd.rewardName().equals(rewardName)) return rwd;
        }
        return null;
    }

    private void resetSessionRewardsBeforeSave() {
        for(RewardData rwd:receivedTimestamps.keySet()){
            if(rwd.isRepeating() && rwd.type()==RewardType.SESSION_TIME) receivedTimestamps.put(rwd, null);
        }
    }

    public boolean checkReward(Reward rw){
        if(!hasData()) return false;
        RewardData rwd = getRewardDataFromName(rw.getName());
        if(rwd==null) return false;
        long oldTimestamp = receivedTimestamps.get(rwd);
        if(oldTimestamp==-1 && !rw.isRepeating()) return false; //Value = -1L means Reward already claimed for non Repeating Rewards
        long nowTimestamp = 0;
        RewardType type = rw.getType();
        boolean grant;
        switch (type) {
            case PLAYTIME -> nowTimestamp = Utils.getPlaytimeInMS(p());
            case SESSION_TIME -> nowTimestamp = getSessionTime();
            case GLOBAL_TIME -> nowTimestamp = System.currentTimeMillis();
        }
        boolean permissionIsOK = !rw.needsPermission() || (rw.needsPermission() && p().hasPermission(rw.getPermissionNeeded()));
        grant = nowTimestamp >= oldTimestamp + rw.getTimeMs() && permissionIsOK;
        if(grant) {
            //Save Repeating Rewards with their Timestamp,
            //Save non Repeating Rewards with -1L
            if(rw.isRepeating()){
                receivedTimestamps.put(rwd, nowTimestamp);
            }else{
                receivedTimestamps.put(rwd, -1L);
            }
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
