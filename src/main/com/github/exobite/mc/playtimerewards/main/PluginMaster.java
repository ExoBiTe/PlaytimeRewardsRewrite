package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.external.authme.AuthMeManager;
import com.github.exobite.mc.playtimerewards.external.vault.VaultPermManager;
import com.github.exobite.mc.playtimerewards.utils.AdvancementManager;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.listeners.PlaytimeCommand;
import com.github.exobite.mc.playtimerewards.listeners.PlaytimeRewardsCommand;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.*;
import com.github.exobite.mc.playtimerewards.web.GenericAPI;
import com.github.exobite.mc.playtimerewards.web.MotdReader;
import com.github.exobite.mc.playtimerewards.external.placeholderapi.PAPIManager;
import com.github.exobite.mc.playtimerewards.listeners.Listeners;
import com.github.exobite.mc.playtimerewards.listeners.PlaytimetopCommand;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bstats.bukkit.Metrics;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PluginMaster extends JavaPlugin {

    private static PluginMaster instance;

    public static PluginMaster getInstance() {
        return instance;
    }

    public static void sendConsoleMessage(Level level, @NotNull String msg){
        String[] parts = msg.split("\n");
        for (String part : parts) {
            instance.getLogger().log(level, part);
        }
    }

    private Version bukkitVersion;
    private boolean pauseAsyncTimer = false;
    private boolean hookedIntoPapi;
    private boolean hookedIntoAuthMe;
    private boolean hookedIntoVault;

    //Constants
    private final int BSTATS_ID = 14369;
    private final Version MIN_VERSION = new Version(1, 17, 0).hidePatch(true);
    private final Version MAX_VERSION = new Version(1, 19, 0).hidePatch(true);

    @Override
    public void onEnable(){
        //Start Time measuring & Setup singleton instance
        long t1 = System.currentTimeMillis();
        instance = this;

        bukkitVersion = VersionHelper.getBukkitVersionNoPatch();
        if(VersionHelper.isSmaller(bukkitVersion, MIN_VERSION)) {
            //Server too old, stop Plugin.
            sendConsoleMessage(Level.SEVERE, "This Plugin doesnt support your Server Version.\nPlease run at least Version "+MIN_VERSION+"!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if(VersionHelper.isLarger(bukkitVersion, MAX_VERSION)) {
            sendConsoleMessage(Level.WARNING, "Detected a newer Version than "+MAX_VERSION+"\nPlease be aware that this Version has not been tested by the Developer!");
        }

        Utils.registerUtils(this);
        Config.registerConfig(this, true);
        ReflectionHelper.getInstance();
        if(Config.getInstance().allowDebugTools()) ExoDebugTools.registerDebugTools(this);
        GUIManager.registerGUIManager(this);
        Lang.registerLangManager(this);
        PlayerManager.registerPlayerManager(this);
        RewardManager.setupRewardManager(this);
        MojangAPI.register(this);
        AFKManager.register(this);
        GenericAPI.register(this);
        //Load Metrics
        setupMetrics();
        //Load Game-Interaction Stuff
        getCommand("Playtime").setExecutor(new PlaytimeCommand());
        getCommand("PlaytimeTop").setExecutor(new PlaytimetopCommand());
        getCommand("PlaytimeRewards").setExecutor(new PlaytimeRewardsCommand());
        getServer().getPluginManager().registerEvents(new Listeners(), this);

        loadExternals();

        //Removed for release Versions
        //AdvancementManager.register();

        //reload support, check for online Players in onEnable & create playerData for them.
        if(!Bukkit.getOnlinePlayers().isEmpty()){
            for(Player p:Bukkit.getOnlinePlayers()){
                PlayerManager.getInstance().createPlayerData(p);
            }
        }

        //Start Optionals, Async stuff
        MotdReader.createMotdReader(this, false);
        if(Config.getInstance().checkForUpdate()) AutoUpdater.createAutoUpdater(this, false);
        startAsyncChecker();

        sendConsoleMessage(Level.INFO, "Plugin is running (took " + (System.currentTimeMillis() - t1) +"ms)!");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        if(PlayerManager.getInstance()!=null) PlayerManager.getInstance().cleanAllPlayerData();
        if(RewardManager.getInstance()!=null) RewardManager.getInstance().saveData();
        if(Config.getInstance()!=null && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance()!=null)
            AutoUpdater.getInstance().moveUpdate();
        //Removed for release Version
        //if(AdvancementManager.getInstance()!=null) AdvancementManager.getInstance().deleteAdvancementFiles();
    }

    public void reloadConfigurationData(@Nullable final CommandSender feedback) {
        reloadConfigurationData(false, feedback);
    }

    public void reloadConfigurationData(final boolean forceRewardReload, @Nullable final CommandSender feedback){
        new BukkitRunnable() {
            @Override
            public void run() {
                PluginMaster.sendConsoleMessage(Level.INFO, "Reloading the Configuration Files...");
                long ms = System.currentTimeMillis();
                pauseAsyncTimer = true; //Pause the Clock while reloading data
                Config.reloadConfig(true);
                Lang.reloadLang();
                if(!RewardManager.reloadRewards(forceRewardReload) && forceRewardReload) {
                    sendConsoleMessage(Level.WARNING, "Couldn't reload the Rewards while they are being edited.");
                }
                AFKManager.getInstance().reloadConfig();
                ExoDebugTools.unregister();
                if(Config.getInstance().allowDebugTools()) ExoDebugTools.registerDebugTools(instance);
                pauseAsyncTimer = false;
                PluginMaster.sendConsoleMessage(Level.INFO, "Reload done (took "+(System.currentTimeMillis() - ms)+"ms)!");
                if(feedback!=null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            feedback.sendMessage(Lang.getInstance().getMessage(Msg.CMD_SUC_PTR_RELOAD_SUCCESS));
                        }
                    }.runTask(instance);
                }
            }
        }.runTaskAsynchronously(this);
    }

    public Version getBukkitVersion(){
        return bukkitVersion;
    }

    public boolean pauseAsyncTimers(){
        return pauseAsyncTimer;
    }

    private void setupMetrics() {
        if(!Config.getInstance().allowMetrics()) return;
        new Metrics(this, BSTATS_ID);
        //No Custom Charts for now.
    }

    private void startAsyncChecker() {
        final AuthMeManager authMe = Bukkit.getPluginManager().getPlugin("AuthMe") != null ? AuthMeManager.getInstance() : null;
        BukkitRunnable br = new BukkitRunnable() {

            private final int PLAYERS_PER_CYCLE = 50;
            private Queue<UUID> playerQueue;
            private boolean createNewQueue = true;

            @Override
            public void run() {
                if(pauseAsyncTimer) return;
                if(createNewQueue) {
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    if(players.isEmpty()) return;   //Don't do anything when there are no Players online
                    playerQueue = new ArrayDeque<>(players.stream().map(Entity::getUniqueId).toList());
                    createNewQueue = false;
                }
                for(int i = 0; i< PLAYERS_PER_CYCLE; i++){
                    UUID id = playerQueue.poll();
                    if(id==null) {
                        //If Queue contains/returns null, create new queue
                        createNewQueue = true;
                        break;
                    }
                    Player p = Bukkit.getPlayer(id);
                    if(p==null){
                        continue;
                    }
                    if(authMe!=null && !authMe.isAuthenticated(p)) return;  //Only grant Permissions to logged in Players
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

    private void loadExternals() {
        //Load Placeholderapi, if it exists
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PAPIManager.register(this);
            hookedIntoPapi = true;
        }

        //Load AuthMe, if it exists
        if(Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            AuthMeManager.register(this);
            hookedIntoAuthMe = true;
        }

        //Load Vault, if it exists
        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            VaultPermManager.register(this);
            hookedIntoVault = true;
        }
    }

    public boolean isHookedIntoPapi() {
        return hookedIntoPapi;
    }

    public boolean isHookedIntoAuthMe() {
        return hookedIntoAuthMe;
    }

    public boolean isHookedIntoVault() {
        return hookedIntoVault;
    }
}
