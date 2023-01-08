package com.github.exobite.mc.playtimerewards.external.placeholderapi;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum Placeholder {

    PLAYTIME(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            String[] param = getTimeValues(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
            return Lang.getInstance().getMessage(Msg.EXT_PAPI_TIME_FORMAT, param);
        }
    }),

    SESSIONTIME(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            String[] param = getTimeValues(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
            return Lang.getInstance().getMessage(Msg.EXT_PAPI_TIME_FORMAT, param);
        }
    }),

    PLAYTIME_DAYS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS() / 86400000;
            return String.valueOf(val);
        }
    }),

    PLAYTIME_HOURS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS() / 3600000;
            return String.valueOf(val);
        }
    }),

    PLAYTIME_MINUTES(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS() / 60000;
            return String.valueOf(val);
        }
    }),

    PLAYTIME_SECONDS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS() / 1000;
            return String.valueOf(val);
        }
    }),

    SESSIONTIME_DAYS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getSessionTime() / 86400000;
            return String.valueOf(val);
        }
    }),

    SESSIONTIME_HOURS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getSessionTime() / 3600000;
            return String.valueOf(val);
        }
    }),

    SESSIONTIME_MINUTES(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getSessionTime() / 60000;
            return String.valueOf(val);
        }
    }),

    SESSIONTIME_SECONDS(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long val = PlayerManager.getInstance().getPlayerData(p).getSessionTime() / 1000;
            return String.valueOf(val);
        }
    }),

    PLAYTIME_DAYS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
            return String.valueOf(values[0]);
        }
    }),

    PLAYTIME_HOURS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
            return String.valueOf(values[1]);
        }
    }),

    PLAYTIME_MINUTES_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
            return String.valueOf(values[2]);
        }
    }),

    PLAYTIME_SECONDS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getPlaytimeMS());
            return String.valueOf(values[3]);
        }
    }),

    SESSIONTIME_DAYS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
            return String.valueOf(values[0]);
        }
    }),

    SESSIONTIME_HOURS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
            return String.valueOf(values[1]);
        }
    }),

    SESSIONTIME_MINUTES_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
            return String.valueOf(values[0]);
        }
    }),

    SESSIONTIME_SECONDS_TRIMMED(new PlaceholderAction() {
        @Override
        String getMessage(Player p) {
            long[] values = Utils.convertTimeMsToLongs(PlayerManager.getInstance().getPlayerData(p).getSessionTime());
            return String.valueOf(values[0]);
        }
    })



    ;



    private final PlaceholderAction action;

    Placeholder(PlaceholderAction action) {
        this.action = action;
    }

    public String getMessage(Player p) {
        return action.getMessage(p);
    }

    public static List<String> getValues() {
        List<String> data = new ArrayList<>();
        for(Placeholder p: values()) {
            data.add(p.name());
        }
        return data;
    }

    @NotNull
    private static String[] getTimeValues(long ms) {
        long[] v = Utils.convertTimeMsToLongs(ms);
        String[] val = new String[4];
        for(int i=0;i<v.length;i++) {
            val[i] = String.valueOf(v[i]);
        }
        return val;
    }

}
