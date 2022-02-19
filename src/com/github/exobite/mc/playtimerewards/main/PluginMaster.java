package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.CodeExec;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.gui.GUIManagerOLD;
import com.github.exobite.mc.playtimerewards.gui.guiHolder;
import com.github.exobite.mc.playtimerewards.listeners.Listeners;
import com.github.exobite.mc.playtimerewards.listeners.Commands;
import com.github.exobite.mc.playtimerewards.utils.ExoDebugTools;
import com.github.exobite.mc.playtimerewards.utils.Message;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PluginMaster extends JavaPlugin implements guiHolder {

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
        setupMetrics();

        sendConsoleMessage(Level.INFO, "Plugin is running (took " + (System.currentTimeMillis() - t1) +"ms)!");

        // /reload support, check for online Players in onEnable & create playerData for them.
        if(Bukkit.getOnlinePlayers().size() > 0){
            for(Player p:Bukkit.getOnlinePlayers()){
                PlayerManager.getInstance().createPlayerData(p);
            }
        }
    }

    public void onDisable() {

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

    public static void sendConsoleMessage(Level level, String msg){
        String prefix = "[" + instance.getDescription().getName() + "] ";
        //Split by \newLine, send all in a seperate message
        String[] parts = msg.split("\n");
        for (String part : parts) {
            instance.log.log(level, prefix + part);
        }
    }

    private void registerGuiManager() {
        final PluginMaster main = this;
        //Utils.fillDefaultFile("guis"+File.separator+"g_warplist.egf");
        GUIManagerOLD.registerGUIManager(this, new CodeExec() {

            @Override
            public Object execCode() {
                GUIManagerOLD inst = (GUIManagerOLD) this.getParam();

                inst.new HolderInstanceInfo("GLOBAL"){

                    @Override
                    protected guiHolder getInstance(Object source) {
                        return main;
                    }
                };

                return null;
            }

        });
    }

    private void setupMetrics() {
        Metrics m = new Metrics(this, bstatsID);
        //No Custom Charts for now.
    }


    @Override
    public Map<String, GUIManagerOLD.GUI> getGuis() {
        return null;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return null;
    }

    @Override
    public void addGui(String internalName, GUIManagerOLD.GUI Gui) {

    }
}
