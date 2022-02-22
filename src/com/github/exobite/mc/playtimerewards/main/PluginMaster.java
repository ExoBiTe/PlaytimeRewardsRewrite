package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.listeners.Listeners;
import com.github.exobite.mc.playtimerewards.listeners.Commands;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PluginMaster extends JavaPlugin {

    private static PluginMaster instance;

    private OLD_VerResult result;
    private Logger log;

    private final char COLOR_CODE = '§';

    private final int bstatsID = 14369;

    /*
    =======BETA RELEASE VERSION=======
    - Removing the DebugTools from the Release Version, too unsafe to put something like that out
    - The Config.yml is unused and not even generated right now
    - bStats Metrics is included as a class file (copied from its github), will get later moved to shade it into the jar using maven
    -

     */

    public void onEnable(){
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

        ReflectionHelper.getInstance();

        //ExoDebugTools.registerDebugTools(this); DebugTools turned off in Release Versions
        GUIManager.registerGUIManager(this);

        getCommand("Playtime").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        Utils.registerUtils(this);
        Lang.registerLangManager(this);

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

    public static PluginMaster getInstance() {
        return instance;
    }

    public static char getColorCode(){
        if(instance==null) return '§';  //Default char
        return instance.COLOR_CODE;
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
        Metrics m = new Metrics(this, bstatsID);
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
