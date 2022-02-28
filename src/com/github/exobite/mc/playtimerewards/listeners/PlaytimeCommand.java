package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.utils.APIReturnAction;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.MojangAPI;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeCommand implements CommandExecutor {

    private static final String ERRNOCONSOLECMD = ChatColor.RED + "Sorry, this ain't a console Command.";
    private static final int MAX_REQUESTS_PER_INTERVAL = 6;
    private static final long MS_PER_INTERVAL = 60000;  //One Minute

    private Map<UUID, Integer> requestsSinceLastReset = new HashMap<>();
    private long lastReset = System.currentTimeMillis();

    private boolean allowRequest(UUID id){
        long msnow = System.currentTimeMillis();
        if(msnow >= lastReset + MS_PER_INTERVAL) {
            lastReset = msnow;
            requestsSinceLastReset.clear();
        }
        //Console uses id=null, can always to requests
        if(id==null) return true;
        int requestsDone = requestsSinceLastReset.getOrDefault(id, 0);
        if(requestsDone>=MAX_REQUESTS_PER_INTERVAL) {
            return false;
        }
        requestsSinceLastReset.put(id, requestsDone+1);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        String rVal;
        if(args.length==0 && sender.hasPermission("playtimerewards.cmd.playtime.own")) {
            if(!(sender instanceof Player p)) {
                rVal = ERRNOCONSOLECMD;
            }else{
                rVal = getPlaytimeStringForPlayerOwn(p);
            }
        }else if(args.length>=1 && sender.hasPermission("playtimerewards.cmd.playtime.other")) {

            //Validate user Input: Minecraft names consist of Letters a-z(A-Z), Numbers 0-9 and underscore
            //They need a Minimum of 3 Chars and have a Maximum of 16 Chars
            String unallowedChars = args[0].replaceAll("^[a-zA-Z0-9_]{3,16}$", "");
            if(!unallowedChars.equals("")) {
                //Not a valid username
                sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]));
                return true;
            }

            Player p = Bukkit.getPlayer(args[0]);
            if(p==null){
                if(!sender.hasPermission("playtimerewards.cmd.playtime.other.offline")) {
                    sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]));
                    return true;
                }
                //Check if something is cached, no request needed
                UUID id = MojangAPI.getInstance().getUUIDFromCachedName(args[0]);
                if(id!=null) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                    if(!op.hasPlayedBefore()) {
                        sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]));
                    }else{
                        sender.sendMessage(getPlaytimeStringForOfflinePlayerOther(op));
                    }
                    return true;
                }
                //Check if a request is allowed
                if(!allowRequest((sender instanceof Player) ? ((Player) sender).getUniqueId() : null)) {
                    sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_TOO_MANY_REQUESTS"));
                }else{
                    //try with OfflinePlayer,
                    APIReturnAction action = new APIReturnAction() {
                        @Override
                        public void onFinish(String data) {
                            if(data==null || data.equals("")) {
                                sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]));
                                return;
                            }
                            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(data));
                            if(!op.hasPlayedBefore()) {
                                sender.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]));
                                return;
                            }
                            sender.sendMessage(getPlaytimeStringForOfflinePlayerOther(op));
                        }
                    };
                    MojangAPI.getInstance().getUUIDFromName(args[0], action);
                }
                return true;
            }else{
                rVal = getPlaytimeStringForPlayerOther(p);
            }
        }else{
            rVal = Lang.getInstance().getMessageWithArgs("CMD_ERR_NO_PERMISSION");
        }
        sender.sendMessage(rVal);
        return true;
    }

    private String getPlaytimeStringForPlayerOwn(Player p){
        //Not really nice written tbh
        long t = Utils.getPlaytimeInMS(p);
        long tSes = PlayerManager.getInstance().getPlayerData(p).getSessionTime();
        long[] values = new long[8];    //0-3 Playtime, 4-7 Sessiontime
        System.arraycopy(Utils.convertTimeMsToLongs(t), 0, values, 0, 4);
        System.arraycopy(Utils.convertTimeMsToLongs(tSes), 0, values, 4, 4);
        String[] valuesStr = new String[8];
        int i=0;
        for(long l:values){
            valuesStr[i] = String.valueOf(l);
            i++;
        }
        return Lang.getInstance().getMessageWithArgs("CMD_SUC_PT_OWN", valuesStr);
    }

    private String getPlaytimeStringForPlayerOther(Player p){
        //Not really nice written tbh
        long t = Utils.getPlaytimeInMS(p);
        long tSes = PlayerManager.getInstance().getPlayerData(p).getSessionTime();
        long[] values = new long[8];    //0-3 Playtime, 4-7 Sessiontime
        System.arraycopy(Utils.convertTimeMsToLongs(t), 0, values, 0, 4);
        System.arraycopy(Utils.convertTimeMsToLongs(tSes), 0, values, 4, 4);
        String[] valuesStr = new String[10]; //0 Playername, 1-4 Playtime, 5 Playername, 6-9 Sessiontime
        int i=0;
        valuesStr[0] = valuesStr[5] = p.getDisplayName();
        for(long l:values){
            if(i<4) {
                valuesStr[i+1] = String.valueOf(l);
            }else{
                valuesStr[i+2] = String.valueOf(l);
            }
            i++;
        }
        return Lang.getInstance().getMessageWithArgs("CMD_SUC_PT_OTHER", valuesStr);
    }

    private String getPlaytimeStringForOfflinePlayerOther(OfflinePlayer p) {
        long t = p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 * 1000L;
        long[] values = {0, 0, 0, 0, 0, 0, 0, 0};    //0-3 Playtime, 4-7 Sessiontime
        System.arraycopy(Utils.convertTimeMsToLongs(t), 0, values, 0, 4);
        String[] valuesStr = new String[5]; //0 Playername, 1-4 Playtime
        int i=0;
        valuesStr[0] = p.getName();
        for(long l:values){
            if(i<4) {
                valuesStr[i+1] = String.valueOf(l);
            }
            i++;
        }
        return Lang.getInstance().getMessageWithArgs("CMD_SUC_PT_OTHER_OFFLINE", valuesStr);
    }

}
