package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.main.PlayerData;
import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RewardManager implements Listener {

    private static RewardManager instance;

    public static RewardManager getInstance() {
        return instance;
    }

    public static void setupRewardManager(PluginMaster main) {
        if(instance!=null) return;
        instance = new RewardManager(main);
        instance.setup(true);
    }

    public static boolean reloadRewards(boolean force) {
        if(instance.currentEdits.size() > 0 ){
            if(!force) return false;
            for(RewardEdit re:instance.currentEdits.values()) {
                re.forceClose();
            }
            instance.currentEdits.clear();
        }
        HandlerList.unregisterAll(instance);
        RewardManager rwMan = new RewardManager(instance.main);
        //Check if there were changed reward names or if some were added/removed
        List<String> oldRewards = instance.getRegisteredRewardNames();
        List<String> newRewards = rwMan.getRegisteredRewardNames();
        boolean equal = oldRewards.size() == newRewards.size();
        if(equal) {
            for(int i=0;i<oldRewards.size();i++) {
                if(oldRewards.get(i).equals(newRewards.get(i))) {
                    equal = false;
                    break;
                }
            }
        }
        instance = rwMan;
        //If the Rewards changed, update every registered PlayerData
        if(!equal) PlayerManager.getInstance().refreshRewardData();
        return true;
    }

    private final PluginMaster main;
    private final List<Reward> registeredRewards = new ArrayList<>();
    private final Map<UUID, RewardEdit> currentEdits = new HashMap<>();

    private RewardManager(@NotNull PluginMaster main){
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    private void setup(boolean sync) {
        if(sync){
            loadData();
        }else{
            new BukkitRunnable() {
                @Override
                public void run() {
                    loadData();
                }
            }.runTaskAsynchronously(main);
        }
    }

    private void loadData() {
        File f = new File(main.getDataFolder() + File.separator + "rewards.yml");
        if(!f.exists()) {
            //Only create File if it doesn't exist
            main.saveResource("rewards.yml", true);
        }
        int counter = 0;
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        //All root Keys should be a reward
        List<String> alreadyLoadedRewards = new ArrayList<>();
        //System.out.println(alreadyLoadedRewards.toString() + " contains "+key+"?");
        for(String key:conf.getKeys(false)) {
            if(!alreadyLoadedRewards.contains(key)) {
                alreadyLoadedRewards.add(key);
                Reward rw = createRewardFromYaml(conf.getConfigurationSection(key));
                if(rw!=null){
                    counter++;
                    registeredRewards.add(rw);
                }
            }else{
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Duplicate Reward '"+key+"'!");
            }
        }
        PluginMaster.sendConsoleMessage(Level.INFO, "Loaded "+counter+" rewards!");
    }

    public Reward createRewardFromYaml(ConfigurationSection conf) {
        return Reward.getRewardFromYaml(conf);
    }

    public Reward getRewardFromName(String name){
        for(Reward rw:registeredRewards){
            if(rw.getName().equals(name)) return rw;
        }
        return null;
    }

    public List<RewardData> getRegisteredRewardData(){
        return registeredRewards.stream().map(
                rw -> new RewardData(rw.getName(), rw.getType(), rw.isRepeating(), rw.grantFirst())).collect(Collectors.toList());
    }

    private List<String> getRegisteredRewardNames() {
        return registeredRewards.stream().map(RewardOptions::getName).collect(Collectors.toList());
    }

    public void checkAndGrantRewards(PlayerData pDat){
        for(Reward rw:registeredRewards) {
            boolean granted = pDat.checkReward(rw);
            if(granted) {
                rw.grantRewardToPlayer(pDat.p());
            }
        }
    }

    public RewardEdit startRewardEdit(Reward rw, Player p){
        if(rw==null || p==null) return null;
        if(rw.isInEdit()) throw new IllegalArgumentException("Can't edit a Reward that is already being edited!");
        if(currentEdits.containsKey(p.getUniqueId())) throw new IllegalArgumentException("One Player can't edit multiple Rewards!");
        RewardEdit rwd = new RewardEdit(rw, p);
        currentEdits.put(p.getUniqueId(), rwd);
        return rwd;
    }

    protected void removeFromEditMap(UUID id){
        currentEdits.remove(id);
    }

    public boolean areRewardsInEdit() {
        return currentEdits.size() > 0;
    }

    @EventHandler
    private void onQuit(@NotNull PlayerQuitEvent e){
        UUID id = e.getPlayer().getUniqueId();
        if(currentEdits.containsKey(id)) {
            currentEdits.get(id).discardChanges();
            currentEdits.remove(id);
        }
    }

    @EventHandler
    private void onChat(@NotNull AsyncPlayerChatEvent e){
        UUID id = e.getPlayer().getUniqueId();
        if(currentEdits.containsKey(id)) {
            e.setCancelled(true);
            new BukkitRunnable(){

                @Override
                public void run() {
                    currentEdits.get(id).passStringFromChat(e.getPlayer(), e.getMessage());
                }
            }.runTask(main);
        }
    }

}
