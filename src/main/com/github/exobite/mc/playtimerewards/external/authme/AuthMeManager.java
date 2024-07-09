package com.github.exobite.mc.playtimerewards.external.authme;

import com.github.exobite.mc.playtimerewards.main.*;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.web.AutoUpdater;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthMeManager implements Listener {

    public static void register(JavaPlugin mainInst) {
        if(instance==null) instance = new AuthMeManager(mainInst);
    }

    public static AuthMeManager getInstance() {
        return instance;
    }

    private static AuthMeManager instance;

    private final JavaPlugin mainInst;
    private final AuthMeApi authApi;

    private final Map<UUID, Long> loggedInTs = new HashMap<>();

    private AuthMeManager(JavaPlugin mainInst) {
        this.mainInst = mainInst;
        authApi = AuthMeApi.getInstance();
        mainInst.getServer().getPluginManager().registerEvents(this, mainInst); //Register AuthMe Event
    }

    public boolean isAuthenticated(Player p) {
        return authApi.isAuthenticated(p);
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent e){
        loggedInTs.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent e) {
        if(Config.getInstance().enableAfkSystem()) AFKManager.getInstance().onQuit(e);
        PlayerManager.getInstance().onLeave(e);
        loggedInTs.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onAuthMeLogin(LoginEvent e) {
        //Create Player
        PlayerData pDat = PlayerManager.getInstance().createPlayerData(e.getPlayer());
        //TODO: Get Time Difference between Login and Authentication, subtract diff from Players Playtime
        long diffMs = System.currentTimeMillis() - loggedInTs.get(e.getPlayer().getUniqueId());
        loggedInTs.remove(e.getPlayer().getUniqueId());
        if(e.getPlayer().hasPermission("playtimerewards.notifyOnUpdate") && Config.getInstance().checkForUpdate() && AutoUpdater.getInstance().isUpdateAvailable()) {
            String newVersion = AutoUpdater.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(Lang.getInstance().getMessage(Msg.NOTIF_UPDATE_AVAILABLE, newVersion, currentVersion));
        }
    }

}
