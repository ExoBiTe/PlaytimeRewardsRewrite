package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.listeners.Listeners;
import com.github.exobite.mc.playtimerewards.listeners.Commands;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.ExoDebugTools;
import com.github.exobite.mc.playtimerewards.utils.Message;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PluginMaster extends JavaPlugin {

    private static PluginMaster instance;

    private VER_RESULT result;
    private Logger log;

    private final int bstatsID = 14369;

    public void onEnable(){
        long t1 = System.currentTimeMillis();
        instance = this;
        //Register Logger
        log = Logger.getLogger(getDescription().getName());

        //Check for Server Version, Deactivate Plugin upon Errors
        result = VersionCheck.canRun(this);
        if(result == VER_RESULT.UNKNOWN_VERSION) {
            //Unknown Version, stop Plugin
            sendConsoleMessage(Level.SEVERE, "This Plugin couldnt detect your Server Version.\n" +
                    "Are you using a custom Server jar?\n" +
                    "Please send this ErrorLog to the Developer\n" +
                    "Bukkit Version: " + getServer().getBukkitVersion());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }else if(result == VER_RESULT.OUTDATED) {
            //Version not supported, stop Plugin
            sendConsoleMessage(Level.SEVERE, "This Plugin doesnt support your Server Version.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }else if(result == VER_RESULT.NO_PARTICLES_SOUND) {
            //Plugin runs, but cant use some of its Features
            sendConsoleMessage(Level.INFO, "The Plugin runs, but doesnt support all of its Features.\n" +
                    "To make sure Everything works, upgrade your Server to Version "+VER_RESULT.UP_TO_DATE);
        }
        if(!setPlaytimeStatisticName()) {
            //No Errors in the Version, but can´t parse the Statistic Name
            sendConsoleMessage(Level.SEVERE,
                    "The Plugin couldnt detect the Statistic Name for the Players Playtime.\n" +
                    "Do you use a custom Server Jar?" +
                    "Please send this Errorlog to the Developer\n" +
                    "Bukkit Version" + getServer().getBukkitVersion() +", "+result.toString());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ExoDebugTools.registerDebugTools(this);
        //registerGuiManager();
        GUIManager.registerGUIManager(this);


        getCommand("Playtime").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        Utils.registerUtils(this);
        Message.registerMessages(this);

        RewardManager.setupRewardManager(this);

        setupMetrics();

        sendConsoleMessage(Level.INFO, "Plugin is running (took " + (System.currentTimeMillis() - t1) +"ms)!");

        //reload support, check for online Players in onEnable & create playerData for them.
        if(Bukkit.getOnlinePlayers().size() > 0){
            for(Player p:Bukkit.getOnlinePlayers()){
                PlayerManager.getInstance().createPlayerData(p);
            }
        }

        startAsyncChecker();
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        PlayerManager.getInstance().cleanAllPlayerData();
    }

    private boolean setPlaytimeStatisticName(){
        String playtimeStatisticName = result == VER_RESULT.UP_TO_DATE ? "PLAY_ONE_MINUTE" : "PLAY_ONE_TICK";
        Statistic s = null;
        try {
            s = Statistic.valueOf(playtimeStatisticName);
        }catch(IllegalArgumentException e){
            //Do nothing.
        }
        return s != null;
    }

    public static PluginMaster getInstance() {
        return instance;
    }

    public static void sendConsoleMessage(Level level, String msg){
        String prefix = "[" + instance.getDescription().getName() + "] ";
        //Split by \newLine, send all in a seperate message
        String[] parts = msg.split("\n");
        for (String part : parts) {
            instance.log.log(level, prefix + part);
        }
    }

    private void setupMetrics() {
        //Metrics for now disabled
        //Metrics m = new Metrics(this, bstatsID);
        //No Custom Charts for now.
    }

    private void startAsyncChecker() {
        BukkitRunnable br = new BukkitRunnable() {

            private final int playersPerCycle = 100;
            private Queue<Player> playerQueue;
            private boolean createNewQueue = true;


            @Override
            public void run() {
                long ms1 = System.currentTimeMillis();
                if(createNewQueue) {
                    playerQueue = new ArrayDeque<>(Bukkit.getOnlinePlayers());
                    createNewQueue = false;
                }
                for(int i=0;i<playersPerCycle;i++){
                    Player p = playerQueue.poll();
                    if(p==null){
                        createNewQueue = true;
                        break;
                    }
                    PlayerData pDat = PlayerManager.getInstance().getPlayerData(p);
                    if(pDat!=null) {
                        RewardManager.getInstance().checkAndGrantRewards(pDat);
                    }
                }
                //sendConsoleMessage(Level.INFO, "One Reward Checking loop took "+(System.currentTimeMillis() - ms1));
            }
        };
        //Run the Task every 20Ticks -> 1 Second
        br.runTaskTimerAsynchronously(this, 20L, 20L).getTaskId();
    }

}
