package com.github.exobite.mc.playtimerewards.external.vault;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.Bukkit.getServer;

public class VaultPermManager {

    public static void register(JavaPlugin mainInst) {
        if(instance==null) instance = new VaultPermManager(mainInst);
    }

    public static VaultPermManager getInstance() {
        return instance;
    }

    private static VaultPermManager instance;

    private final JavaPlugin mainInst;
    private Permission vaultPerms;

    private VaultPermManager(JavaPlugin mainInst) {
        this.mainInst = mainInst;
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        vaultPerms = rsp.getProvider();
    }

    public boolean offlinePlayerHasPerm(World w, OfflinePlayer op, String permission) {
        return vaultPerms.playerHas(w.getName(), op, permission);
    }

}
