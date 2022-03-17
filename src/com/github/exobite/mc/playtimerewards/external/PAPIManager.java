package com.github.exobite.mc.playtimerewards.external;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class PAPIManager extends PlaceholderExpansion {

    private static final String[] PLACEHOLDERS = {"playtime", "sessiontime"};
    private static final String IDENTIFIER = "ptr";

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
        return Arrays.asList(PLACEHOLDERS);
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String params) {
        switch(params.toLowerCase(Locale.ROOT)) {
            case "playtime" -> {
                String[] param = getTimeValues(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
                return Lang.getInstance().getMessage(Msg.EXT_PAPI_TIME_FORMAT, param);
            }

            case "sessiontime" -> {
                String[] param = getTimeValues(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
                return Lang.getInstance().getMessage(Msg.EXT_PAPI_TIME_FORMAT, param);
            }

            default -> {
                return null;
            }
        }
    }

    @NotNull
    private String [] getTimeValues(long ms) {
        long[] v = Utils.convertTimeMsToLongs(ms);
        String[] val = new String[4];
        for(int i=0;i<v.length;i++) {
            val[i] = String.valueOf(v[i]);
        }
        return val;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return IDENTIFIER.toLowerCase(Locale.ROOT);
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
