package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.listeners.Listeners;
import com.github.exobite.mc.playtimerewards.listeners.Commands;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.*;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import com.github.exobite.mc.playtimerewards.web.MotdReader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PluginMaster extends JavaPlugin {

    private static PluginMaster instance;

    private Logger log;

    //Constants
    private final int BSTATS_ID = 14369;

    /*
    =======BETA RELEASE VERSION=======
    - The Config.yml is unused and not even generated right now
    - bStats Metrics is included as a class file (copied from its github), will get later moved to shade it into the jar using maven

     */

    public void onEnable(){
        //Start Time measuring & Setup singleton instance
        long t1 = System.currentTimeMillis();
        instance = this;
        //Register Logger
        log = Logger.getLogger(getDescription().getName());

        //Call both getInstance to create the singleton instance
        //Call VersionHelper first, as ReflectionHelper uses it
        VersionIdentifier.getInstance();

        if(VersionIdentifier.getInstance().isSmaller(1, 17, 0)) {
            //Server too old, stop Plugin.
            sendConsoleMessage(Level.SEVERE, "This Plugin doesnt support your Server Version.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Utils.registerUtils(this);
        Config.registerConfig(this, false);
        ReflectionHelper.getInstance();
        if(Config.getInstance().allowDebugTools()) ExoDebugTools.registerDebugTools(this);
        GUIManager.registerGUIManager(this);
        Lang.registerLangManager(this);
        RewardManager.setupRewardManager(this);
        //Load Metrics
        setupMetrics();
        //Load Game-Interaction Stuff
        getCommand("Playtime").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        //reload support, check for online Players in onEnable & create playerData for them.
        if(Bukkit.getOnlinePlayers().size() > 0){
            for(Player p:Bukkit.getOnlinePlayers()){
                PlayerManager.getInstance().createPlayerData(p);
            }
        }

        //Start Optional, Async stuff
        MotdReader.createMotdReader(this, false);
        if(Config.getInstance().checkForUpdate()) AutoUpdater.createAutoUpdater(this, false);
        startAsyncChecker();

        sendConsoleMessage(Level.INFO, "Plugin is running (took " + (System.currentTimeMillis() - t1) +"ms)!");
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        PlayerManager.getInstance().cleanAllPlayerData();
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
        new Metrics(this, BSTATS_ID);
        //No Custom Charts for now.
    }

    private void startAsyncChecker() {
        BukkitRunnable br = new BukkitRunnable() {

            private final int PLAYERS_PER_CYCLE = 100;
            private Queue<Player> playerQueue;
            private boolean createNewQueue = true;


            @Override
            public void run() {
                if(createNewQueue) {
                    playerQueue = new ArrayDeque<>(Bukkit.getOnlinePlayers());
                    createNewQueue = false;
                }
                for(int i = 0; i< PLAYERS_PER_CYCLE; i++){
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
            }
        };
        //Run the Task every 20Ticks -> 1 Second
        br.runTaskTimerAsynchronously(this, 20L, 20L).getTaskId();
    }

}
