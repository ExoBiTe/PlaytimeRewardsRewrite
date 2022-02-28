package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
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

    private final String CONF_FILENAME = "config.yml";

    private final JavaPlugin main;

    //Config values
    private boolean checkForUpdate = true,
            allowAutoDownload = false,
            allowDebugTools = false;
    private long autoSaveTimerMS = 30 * 60000;   //30 Minutes
    private char colorCode = '§';
    private int playtimetopamount = 10;


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
        if(!f.exists()) {
            PluginMaster.sendConsoleMessage(Level.INFO, "Couldn't find a Config.yml, generated a new one.");
            main.saveResource(CONF_FILENAME, true);
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        if(conf.getKeys(true).size() <= 0) {
            //No Config or empty Config?
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't load the config.yml, loaded Defaults.\nIs the File existing and not empty?");
            return;
        }
        //Read Data from File
        checkForUpdate = conf.getBoolean("EnableUpdateCheck", true);
        allowAutoDownload = conf.getBoolean("AllowAutoUpdate", true);
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
                }else{
                    //It's okay, but let's inform the Console...
                    PluginMaster.sendConsoleMessage(Level.WARNING, "The Auto Save Interval is less than a Minute("+saveTimerIntervalStr+"). Is that correct?");
                }
            }
        }

        //Hidden values
        allowDebugTools = conf.getBoolean("debug_allowDebugTools", false);  //Hidden in default config
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

    public char getColorCode(){
        return colorCode;
    }

    public long getAutoSaveTimerMS(){
        return autoSaveTimerMS;
    }

    public int getPlaytimetopamount(){
        return playtimetopamount;
    }



}
