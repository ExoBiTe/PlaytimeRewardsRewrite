package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private static final String ERRNOCONSOLECMD = ChatColor.RED + "Sorry, this ain't a console Command.";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage(ERRNOCONSOLECMD);
            return true;
        }
        if(!p.hasPermission("timerewards.cmd.playtime.own")) {
            sender.sendMessage(Lang.getInstance().getMessageWithArgs("MD_ERR_NO_PERMISSION"));
            return true;
        }
        //Not really nice written tbh
        long t = Utils.getPlaytimeInMS(p);
        long tSes = PlayerManager.getInstance().getPlayerData(p).getSessionTime();
        long[] values = new long[8];    //1-4 Playtime, 5-8 Sessiontime
        System.arraycopy(Utils.convertTimeMsToLongs(t), 0, values, 0, 4);
        System.arraycopy(Utils.convertTimeMsToLongs(tSes), 0, values, 4, 4);
        String[] valuesStr = new String[8];
        int i=0;
        for(long l:values){
            valuesStr[i] = String.valueOf(l);
            i++;
        }
        p.sendMessage(Lang.getInstance().getMessageWithArgs("PTR_SUC_PLAYTIMECOMMAND", valuesStr));
        return true;
    }

}
