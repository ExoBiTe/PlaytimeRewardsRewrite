package com.github.exobite.mc.playtimerewards.listeners;

import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.rewards.Reward;
import com.github.exobite.mc.playtimerewards.rewards.RewardData;
import com.github.exobite.mc.playtimerewards.rewards.RewardManager;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
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
    private static final String CMD_USAGE_INFO =
            ChatColor.DARK_AQUA+"/Playtimerewards info"+ChatColor.GRAY+" -- "+ChatColor.AQUA+"Shows Information about the Plugin";

    private static final String PTR_LIST_PERM = "playtimerewards.cmd.playtimerewards.list";
    private static final String PTR_EDIT_PERM = "playtimerewards.cmd.playtimerewards.editreward";
    private static final String PTR_RELOAD_PERM = "playtimerewards.cmd.playtimerewards.reload";
    private static final String PTR_INFO_PERM = "playtimerewards.cmd.playtimerewards.info";

    private void sendHelpText(CommandSender s) {
        StringBuilder sb = new StringBuilder(CMD_USAGE);
        if(s.hasPermission(PTR_LIST_PERM)) sb.append("\n").append(CMD_USAGE_LIST);
        if(s.hasPermission(PTR_EDIT_PERM)) sb.append("\n").append(CMD_USAGE_REWARDEDIT);
        if(s.hasPermission(PTR_RELOAD_PERM)) sb.append("\n").append(CMD_USAGE_RELOAD);
        if(s.hasPermission(PTR_INFO_PERM)) sb.append("\n").append(CMD_USAGE_INFO);
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
                case "info" -> infoCommand(sender);
                case "setplaytime" -> setPlaytimeCommand(sender, args);
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
    }

    private void infoCommand(@NotNull CommandSender s) {
        if(!s.hasPermission(PTR_INFO_PERM)) {
            s.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_NO_PERMISSION));
            return;
        }
        PluginMaster inst = PluginMaster.getInstance();
        StringBuilder msg = new StringBuilder().append(ChatColor.DARK_AQUA);

        msg.append("Running ").append(ChatColor.AQUA).append(inst.getDescription().getName());
        msg.append(ChatColor.GRAY).append("v").append(ChatColor.AQUA).append(inst.getDescription().getVersion());
        msg.append(ChatColor.DARK_AQUA).append(" by ").append(ChatColor.GOLD).append("ExoBiTe");
        msg.append(ChatColor.GRAY);
        //PAPI
        msg.append("\n").append(inst.isHookedIntoPapi() ? ChatColor.GREEN : ChatColor.DARK_GRAY);
        msg.append(inst.isHookedIntoPapi() ? "Is " : "Isn't ").append("hooked into PlaceholderAPI.");
        /*
        //AuthMe
        msg.append("\n").append(inst.isHookedIntoAuthMe() ? ChatColor.GREEN : ChatColor.DARK_GRAY);
        msg.append(inst.isHookedIntoAuthMe() ? "Is " : "Isn't ").append("hooked into AuthMe.");
        //Vault
        msg.append("\n").append(inst.isHookedIntoVault() ? ChatColor.GREEN : ChatColor.DARK_GRAY);
        msg.append(inst.isHookedIntoVault() ? "Is " : "Isn't ").append("hooked into Vault.");
        */

        msg.append(ChatColor.GRAY).append("\nWebsite: ").append(ChatColor.DARK_AQUA).append(inst.getDescription().getWebsite());

        s.sendMessage(msg.toString());
    }

    private void setPlaytimeCommand(@NotNull CommandSender sender, String ... args) {
        // /ptr setPlaytime <timeString> [player]
        //TODO: Add Permission - and check that Perm, too!
        if((args.length<=3 && !(sender instanceof Player)) || args.length <= 2) {
            sender.sendMessage("Not enough args!");
            return;
        }
        long timeMs = Utils.convertTimeStringToMS(args[1]);
        Player target;
        if(sender instanceof Player) target = (Player) sender;
        else target = Bukkit.getPlayer(args[2]);
        if(target == null) {
            sender.sendMessage(Lang.getInstance().getMessage(Msg.CMD_ERR_PLAYER_NOT_FOUND, args[2]));
            return;
        }
        Utils.setPlaytimeToTimeMs(target, timeMs);
        //TODO: Create Msg!
        sender.sendMessage("Set the Playtime of "+target.getName()+" to "+args[1]+"("+timeMs+"ms)!");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        List<String> data = new ArrayList<>();
        int size = args.length;
        //TODO: Add TabCompleter for setPlaytime
        if(size<2) {
            if(sender.hasPermission(PTR_LIST_PERM)) data.add("list");
            if(sender.hasPermission(PTR_EDIT_PERM)) data.add("editReward");
            if(sender.hasPermission(PTR_RELOAD_PERM)) data.add("reload");
            if(sender.hasPermission(PTR_INFO_PERM)) data.add("info");
        }else if(size==2 && args[0].equalsIgnoreCase("editreward")) {
            for(RewardData rwd:RewardManager.getInstance().getRegisteredRewardData()) {
                if(rwd.rewardName().startsWith(args[1])) {
                    data.add(rwd.rewardName());
                }
            }
        }
        return data;
    }

}
