package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.logging.Level;

public class Config {

    private static Config instance;

    public static Config getInstance() {
        return instance;
    }

    public static Config registerConfig(JavaPlugin main, boolean sync) {
        if(instance==null) {
            instance = new Config(main);
            instance.start(sync);
        }
        return instance;
    }

    public static void reloadConfig(boolean sync) {
        Config newInst = new Config(instance.main);
        newInst.start(sync);
        instance = newInst;
    }

    private final String CONF_FILENAME = "config.yml";

    private final JavaPlugin main;

    //Config values
    private boolean checkForUpdate = true,
            allowAutoDownload = false,
            allowMetrics = true,
            allowDebugTools = false;

    private long autoSaveTimerMS = 30 * 60000;   //30 Minutes
    private char colorCode = '§';
    private int playtimetopamount = 10;

    private boolean enableAfkSystem = false;
    private long afkTime = 60 * 5;      //5 Minutes
    private boolean cancelAfkOnMove;
    private boolean cancelAfkOnLook;
    private boolean cancelAfkOnInteract;
    private boolean cancelAfkOnChat;
    private boolean cancelAfkOnCommand;


    private Config(JavaPlugin main) {
        this.main = main;
    }

    private void start(boolean sync) {
        if(sync) {
            loadConfig();
        }else{
            new BukkitRunnable() {
                @Override
                public void run() {
                    loadConfig();
                }
            }.runTask(main);
        }
    }

    private void loadConfig(){
        File f = new File(main.getDataFolder() + File.separator + CONF_FILENAME);
        boolean filechanged = Utils.updateFileVersionDependent(CONF_FILENAME);
        if(filechanged) PluginMaster.sendConsoleMessage(Level.INFO, "Your "+CONF_FILENAME+" got updated!");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        if(conf.getKeys(true).size() <= 0) {
            //No Config or empty Config?
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't load the config.yml, loaded Defaults.\nIs the File existing and not empty?");
            return;
        }
        //Read Data from File
        checkForUpdate = conf.getBoolean("EnableUpdateCheck", true);
        String saveTimerIntervalStr = conf.getString("DataSaveInterval", "30m");
        if(saveTimerIntervalStr.equals("-1")) {
            PluginMaster.sendConsoleMessage(Level.INFO, "Auto Saving the PlayerData is disabled from the Config.");
            autoSaveTimerMS = -1L;  //Auto Save Disabled
        }else{
            autoSaveTimerMS = Utils.convertTimeStringToMS(saveTimerIntervalStr);
            if(autoSaveTimerMS<60000) { //Auto Save Interval is less than a Minute? Strange...
                if(autoSaveTimerMS<=0) {
                    //Error at parsing the String, set the Default.
                    autoSaveTimerMS = 30 * 60000;   //30 Minutes
                    PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't parse the Config Value of 'DataSaveInterval': "+saveTimerIntervalStr
                    + "\nThe Value was set to 30 Minutes.");
                }else{
                    //It's okay, but let's inform the Console...
                    PluginMaster.sendConsoleMessage(Level.WARNING, "The Auto Save Interval is less than a Minute('"+saveTimerIntervalStr+"'). Is that correct?");
                }
            }
        }
        playtimetopamount = conf.getInt("PlaytimeTopAmount", 10);

        //AFK System (Hidden for now)
        enableAfkSystem = conf.getBoolean("AFK.Enable", false);
        if(enableAfkSystem) {
            cancelAfkOnMove = conf.getBoolean("AFK.CancelOnMove", false);
            cancelAfkOnLook = conf.getBoolean("AFK.CancelOnLook", false);
            cancelAfkOnInteract = conf.getBoolean("AFK.CancelOnInteract", false);
            cancelAfkOnChat = conf.getBoolean("AFK.CancelOnChat", false);
            cancelAfkOnCommand = conf.getBoolean("AFK.CancelOnCommand", false);
            String afkTimeString = conf.getString("AFK.Time", "5m");
            afkTime = Utils.convertTimeStringToMS(afkTimeString) / 1000;
            if(afkTime <= 0) {
                enableAfkSystem = false;
                if(!afkTimeString.equals("-1")) PluginMaster.sendConsoleMessage(Level.SEVERE,
                        "Disabling the AFK-System, an unknown Value '"+afkTimeString+"' was supplied at 'AFK.Time'.");
            }else if(afkTime < 5){
                //Very low afk Time, let's inform the Console
                PluginMaster.sendConsoleMessage(Level.WARNING, "The Time to get flagged is shorter than 5 Seconds('"+afkTimeString+"') - is this correct?");
            }
        }

        //Hidden values
        allowDebugTools = conf.getBoolean("debug_allowDebugTools", false);  //Hidden in default config
        allowAutoDownload = conf.getBoolean("AllowAutoUpdate", false);
        allowMetrics = conf.getBoolean("AllowMetrics", true);
        String colorStr = conf.getString("ColorCode", "§");
        colorCode = colorStr.charAt(0);
    }

    public boolean checkForUpdate(){
        return checkForUpdate;
    }

    public boolean allowDebugTools() {
        return allowDebugTools;
    }

    public boolean allowAutoDownload(){
        return allowAutoDownload;
    }

    public boolean allowMetrics(){
        return allowMetrics;
    }

    public char getColorCode(){
        return colorCode;
    }

    public long getAutoSaveTimerMS(){
        return autoSaveTimerMS;
    }

    public int getPlaytimetopamount(){
        return playtimetopamount;
    }

    public boolean enableAfkSystem() {
        return enableAfkSystem;
    }

    public boolean isCancelAfkOnMove() {
        return cancelAfkOnMove;
    }

    public boolean isCancelAfkOnLook() {
        return cancelAfkOnLook;
    }

    public boolean isCancelAfkOnInteract() {
        return cancelAfkOnInteract;
    }

    public boolean isCancelAfkOnChat() {
        return cancelAfkOnChat;
    }

    public boolean isCancelAfkOnCommand() {
        return cancelAfkOnCommand;
    }

    public long getAfkTime() {
        return afkTime;
    }



}
