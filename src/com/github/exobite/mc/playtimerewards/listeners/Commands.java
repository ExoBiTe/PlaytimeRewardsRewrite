package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private static final String ERRNOCONSOLECMD = ChatColor.RED + "Sorry, this ain't a console Command.";

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
            Player p = Bukkit.getPlayer(args[0]);
            if(p==null){
                rVal = Lang.getInstance().getMessageWithArgs("CMD_ERR_PLAYER_NOT_FOUND", args[0]);
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

}
