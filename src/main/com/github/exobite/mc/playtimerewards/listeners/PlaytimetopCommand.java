package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlaytimetopCommand implements CommandExecutor {

    private final static long MIN_TIME_FOR_REFRESH = 1000L * 60 * 5; //5 Minutes

    private final static String PTTOP_USE_PERM = "playtimerewards.cmd.playtimetop";
    private final static String PTTOP_HIDE_PERM = "playtimerewards.playtimetop.hide";

    private long lastPolled = 0;
    private Map<String, Integer> cachedTop = new HashMap<>();
    private int lastAmount = 0;

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if(!sender.hasPermission(PTTOP_USE_PERM)) {
            sender.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
            return true;
        }
        sender.sendMessage(listTop());
        return true;
    }

    @NotNull
    private String listTop() {
        //First check if a new calc can be done
        long msnow = System.currentTimeMillis();
        int amount = Config.getInstance().getPlaytimetopamount();
        if(msnow > (lastPolled + MIN_TIME_FOR_REFRESH) || amount != lastAmount) {
            lastPolled = msnow;
            calcPlaytimeTop(amount);
            //debugPtTop(amount);
        }
        String header = Lang.getInstance().getMessage(Msg.CMD_SUC_PTTOP_HEADER, String.valueOf(cachedTop.size()));
        StringBuilder sb = new StringBuilder(header);
        int idx = 1;
        for(String name:cachedTop.keySet()) {
            sb.append("\n").append(getDataEntry(name, cachedTop.get(name), idx));
            idx++;
        }
        return sb.toString();
    }

    private String getDataEntry(String name, int time, int index) {
        long[] times = Utils.convertTimeMsToLongs(time / 20 * 1000L);
        String[] args = new String[6];
        args[0] = String.valueOf(index);
        args[1] = name;
        for(int i=0;i<times.length;i++) {
            args[i+2] = String.valueOf(times[i]);
        }
        return Lang.getInstance().getMessage(Msg.CMD_SUC_PTTOP_ENTRY, args);

    }

    public void calcPlaytimeTop(int amount) {
        lastAmount = amount;
        //Get all Playtimes
        Map<String, Integer> playtimes = new HashMap<>();
        for(OfflinePlayer op: Bukkit.getOfflinePlayers()) {
            playtimes.put(op.getName(), op.getStatistic(Statistic.PLAY_ONE_MINUTE));
        }
        //Sort them into a List
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(playtimes.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        //Put them back into the Map
        Map<String, Integer> result = new LinkedHashMap<>();
        int i = 0;
        for(Map.Entry<String, Integer> e:entries) {
            i++;
            result.put(e.getKey(), e.getValue());
            if(i>amount) break;
        }
        cachedTop = result;
    }

    private final int[] dummyData = {300, 4800, 6500, 20, 300, 4172, 88, 888, 3984, 4387, 48372, 33394, 4923};

    public void debugPtTop(int amount) {
        lastAmount = amount;
        Map<String, Integer> playtimes = new HashMap<>();
        for(int i=0;i< dummyData.length;i++) {
            playtimes.put(i+"", dummyData[i]);
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(playtimes.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        Map<String, Integer> result = new LinkedHashMap<>();
        int i = 0;
        for(Map.Entry<String, Integer> e:entries) {
            i++;
            result.put(e.getKey(), e.getValue());
            if(i>amount) break;
        }
        cachedTop = result;
    }

}
