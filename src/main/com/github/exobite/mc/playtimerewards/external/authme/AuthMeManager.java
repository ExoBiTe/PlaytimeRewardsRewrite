package com.github.exobite.mc.playtimerewards.external.authme;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthMeManager {

    public static void register(JavaPlugin mainInst) {
        if(instance==null) instance = new AuthMeManager(mainInst);
    }

    public static AuthMeManager getInstance() {
        return instance;
    }

    private static AuthMeManager instance;

    private final JavaPlugin mainInst;
    private final AuthMeApi authApi;

    private AuthMeManager(JavaPlugin mainInst) {
        this.mainInst = mainInst;
        authApi = AuthMeApi.getInstance();
    }

    public boolean isAuthenticated(Player p) {
        return authApi.isAuthenticated(p);
    }

}
