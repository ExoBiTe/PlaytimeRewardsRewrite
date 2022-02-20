package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.main.PlayerData;
import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Commands implements CommandExecutor {

    private static String errNoConsoleCmd = ChatColor.RED + "Sorry, this ain´t a console Command.";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(errNoConsoleCmd);
            return true;
        }
        Player p = (Player) sender;
        long t = Utils.getPlaytimeInMS(p);
        long tSes = PlayerManager.getInstance().getPlayerData(p).getSessionTime();
        p.sendMessage("Total Played: "+t + ", " + Utils.formatTimeMsToString(t, "%d %h %m %s"));
        p.sendMessage("Session Played: "+tSes+", "+Utils.formatTimeMsToString(tSes, "%d %h %m %s"));
        return true;
    }

}
