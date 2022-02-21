package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.main.PlayerData;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RewardManager {

    private static RewardManager instance;

    public static RewardManager getInstance() {
        return instance;
    }

    public static void setupRewardManager(PluginMaster main) {
        if(instance!=null) return;
        instance = new RewardManager(main);
        instance.setup(true);
    }

    private final PluginMaster main;
    private List<Reward> registeredRewards = new ArrayList<>();

    private RewardManager(PluginMaster main){
        this.main = main;
    }

    private void setup(boolean sync) {
        BukkitRunnable rb = new BukkitRunnable() {
            @Override
            public void run() {
                File f = new File(main.getDataFolder() + File.separator + "rewards.yml");
                if(!f.exists()) {
                    //Only create File if it doesn't exist
                    Utils.fillDefaultFile("rewards.yml");
                }
                int counter = 0;
                YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                //All root Keys should be a reward
                for(String key:conf.getKeys(false)) {
                    Reward rw = createRewardFromYaml(conf.getConfigurationSection(key));
                    if(rw!=null){
                        counter++;
                        registeredRewards.add(rw);
                    }
                }
                PluginMaster.sendConsoleMessage(Level.INFO, "Loaded "+counter+" rewards!");
            }
        };
        if(sync){
            rb.runTask(main);
        }else{
            rb.runTaskAsynchronously(main);
        }
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
        List<RewardData> data = registeredRewards.stream().map(
                rw -> new RewardData(rw.getName(), rw.getType(), rw.isRepeating(), rw.grantFirst())).collect(Collectors.toList());
        return data;
    }

    public void checkAndGrantRewards(PlayerData pDat){
        for(Reward rw:registeredRewards) {
            boolean granted = pDat.checkReward(rw);
            if(granted) {
                rw.grantRewardToPlayer(pDat.p());
            }
        }
    }



}
