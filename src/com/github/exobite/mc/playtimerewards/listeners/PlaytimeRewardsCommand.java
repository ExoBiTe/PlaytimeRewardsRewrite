package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardData;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class PlaytimeRewardsCommand implements CommandExecutor {

    private static final String ERRNOCONSOLECMD = ChatColor.RED + "Sorry, this ain't a console Command.";

    private static final String CMD_USAGE = ChatColor.GOLD + "Command Usage:";
    private static final String CMD_USAGE_LIST =
            ChatColor.DARK_AQUA+"/Playtimerewards list"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Lists all registered Rewards";
    private static final String CMD_USAGE_REWARDEDIT =
            ChatColor.DARK_AQUA+"/Playtimerewards editReward <rewardname>"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Lists all registered Rewards\n";



    private void sendHelpText(CommandSender s) {
        StringBuilder sb = new StringBuilder(CMD_USAGE);
        if(s.hasPermission("playtimerewards.cmd.playtimerewards.list")) sb.append("\n").append(CMD_USAGE_LIST);
        if(s.hasPermission("playtimerewards.cmd.playtimerewards.editreward")) sb.append("\n").append(CMD_USAGE_REWARDEDIT);
        s.sendMessage(sb.toString());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if(args.length<=0) {
            sendHelpText(sender);
        }else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "list" -> listCommand(sender);
                case "editreward" -> editRewardCommand(sender, args);
                default -> sendHelpText(sender);
            }
        }
        return true;
    }

    private void listCommand(CommandSender s) {
        if(!s.hasPermission("playtimerewards.cmd.playtimerewards.list")) {
            s.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_NO_PERMISSION"));
            return;
        }
        List<RewardData> data = RewardManager.getInstance().getRegisteredRewardData();
        StringBuilder sb = new StringBuilder("Listing "+data.size()+" Rewards:");
        for(RewardData rwd:data) {
            sb.append("\n").append(rwd.rewardName()).append(" is ").append(rwd.type());
        }
        s.sendMessage(sb.toString());
    }

    private void editRewardCommand(CommandSender s, String ... args) {
        if(!s.hasPermission("playtimerewards.cmd.playtimerewards.editreward")) {
            s.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_NO_PERMISSION"));
            return;
        }
        if(!(s instanceof Player p)) {
            s.sendMessage(ERRNOCONSOLECMD);
            return;
        }
        if(args.length<2) {
            s.sendMessage(CMD_USAGE + "\n" + CMD_USAGE_REWARDEDIT);
            return;
        }
        Reward rw = RewardManager.getInstance().getRewardFromName(args[1]);
        if(rw==null) {
            s.sendMessage(Lang.getInstance().getMessageWithArgs("CMD_ERR_REWARD_NOT_FOUND", args[1]));
            return;
        }
        RewardManager.getInstance().startRewardEdit(rw, p);
    }

}
