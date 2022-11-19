package com.github.exobite.mc.playtimerewards.external.placeholderapi;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class PAPIManager extends PlaceholderExpansion {

    private static final String IDENTIFIER = "PTR";

    private static PAPIManager instance;

    public static PAPIManager getInstance() {
        return instance;
    }

    public static PAPIManager register(JavaPlugin main) {
        if(instance==null) {
            instance = new PAPIManager(main);
        }
        return instance;
    }

    private final JavaPlugin main;

    private PAPIManager(JavaPlugin main) {
        this.main = main;
        PluginMaster.sendConsoleMessage(Level.INFO, "Found PlaceholderAPI, registering Placeholders...");
        this.register();
    }

    @Override
    @NotNull
    public List<String> getPlaceholders() {
        return Placeholder.getValues();
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String param) {
        try {
            Placeholder ph  = Placeholder.valueOf(param.toUpperCase(Locale.ROOT));
            return ph.getMessage(p);
        }catch (IllegalArgumentException e){
            //Unknown Placeholder
            PluginMaster.sendConsoleMessage(Level.WARNING, "Unknown Placeholder '"+param+"' has been passed to this Plugin!");
            return null;
        }
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return IDENTIFIER.toUpperCase(Locale.ROOT);
    }

    @Override
    @NotNull
    public String getAuthor() {
        return main.getDescription().getAuthors().get(0);
    }

    @Override
    @NotNull
    public String getVersion() {
        return main.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }


}
