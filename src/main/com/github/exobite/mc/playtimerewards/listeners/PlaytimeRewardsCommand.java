package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardData;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaytimeRewardsCommand implements CommandExecutor, TabCompleter {

    private static final String ERRNOCONSOLECMD = ChatColor.RED + "Sorry, this ain't a console Command.";

    private static final String CMD_USAGE = ChatColor.GOLD + "Command Usage:";
    private static final String CMD_USAGE_LIST =
            ChatColor.DARK_AQUA+"/Playtimerewards list"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Lists all registered Rewards";
    private static final String CMD_USAGE_REWARDEDIT =
            ChatColor.DARK_AQUA+"/Playtimerewards editReward <rewardname>"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Lists all registered Rewards";
    private static final String CMD_USAGE_RELOAD =
            ChatColor.DARK_AQUA+"/PlaytimeRewards reload"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Reloads all Plugin configuration data";

    private static final String PTR_LIST_PERM = "playtimerewards.cmd.playtimerewards.list";
    private static final String PTR_EDIT_PERM = "playtimerewards.cmd.playtimerewards.editreward";
    private static final String PTR_RELOAD_PERM = "playtimerewards.cmd.playtimerewards.reload";

    private void sendHelpText(CommandSender s) {
        StringBuilder sb = new StringBuilder(CMD_USAGE);
        if(s.hasPermission(PTR_LIST_PERM)) sb.append("\n").append(CMD_USAGE_LIST);
        if(s.hasPermission(PTR_EDIT_PERM)) sb.append("\n").append(CMD_USAGE_REWARDEDIT);
        if(s.hasPermission(PTR_RELOAD_PERM)) sb.append("\n").append(CMD_USAGE_RELOAD);
        String msg = sb.toString();
        if(msg.equals(CMD_USAGE)) {
            //No Permissions at all, send no Permission Message
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
        }else {
            s.sendMessage(sb.toString());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if(args.length<=0) {
            sendHelpText(sender);
        }else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "list" -> listCommand(sender);
                case "editreward" -> editRewardCommand(sender, args);
                case "reload" -> reloadCommand(sender);
                default -> sendHelpText(sender);
            }
        }
        return true;
    }

    private void listCommand(@NotNull CommandSender s) {
        if(!s.hasPermission(PTR_LIST_PERM)) {
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
            return;
        }
        List<RewardData> data = RewardManager.getInstance().getRegisteredRewardData();
        StringBuilder sb = new StringBuilder(Lang.getInstance().getMessage(Msg.CMD_SUC_PTR_LIST_HEADER, String.valueOf(data.size())));
        for(RewardData rwd:data) {
            Reward rw = RewardManager.getInstance().getRewardFromName(rwd.rewardName());
            sb.append("\n").append(Lang.getInstance()
                    .getMessage(Msg.CMD_SUC_PTR_LIST_ENTRY, rw.getName(), rw.getDisplayName(), rw.getType().toString()));
        }
        s.sendMessage(sb.toString());
    }

    private void editRewardCommand(@NotNull CommandSender s, String ... args) {
        if(!s.hasPermission(PTR_EDIT_PERM)) {
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
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
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_REWARD_NOT_FOUND, args[1]));
            return;
        }
        RewardManager.getInstance().startRewardEdit(rw, p);
    }

    private void reloadCommand(@NotNull CommandSender s) {
        if(!s.hasPermission(PTR_RELOAD_PERM)) {
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
            return;
        }
        //Check for Rewards in Editing State
        if(RewardManager.getInstance().areRewardsInEdit()) {
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_WARN_PTR_RELOAD));
        }
        PluginMaster.getInstance().reloadConfigurationData(s);
        var test = "";
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> data = new ArrayList<>();
        int size = args.length;
        if(size<2) {
            if(sender.hasPermission(PTR_LIST_PERM)) data.add("list");
            if(sender.hasPermission(PTR_EDIT_PERM)) data.add("editReward");
            if(sender.hasPermission(PTR_RELOAD_PERM)) data.add("reload");
        }else if(size==2) {
            if(args[0].equalsIgnoreCase("editreward")) {
                for(RewardData rwd:RewardManager.getInstance().getRegisteredRewardData()) {
                    if(rwd.rewardName().startsWith(args[1])) {
                        data.add(rwd.rewardName());
                    }
                }
            }
        }
        return data;
    }

}
