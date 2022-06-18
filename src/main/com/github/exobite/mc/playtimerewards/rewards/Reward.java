package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Reward extends RewardOptions {

    protected static Reward getRewardFromYaml(ConfigurationSection cs){
        if(cs==null) return null;
        String name = cs.getName();
        String rwTypeStr = cs.getString("CountType", "");
        RewardType rwType;
        try {
            rwType = RewardType.valueOf(rwTypeStr);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown RewardType '"+rwTypeStr+"' in Reward '"+name+"'");
            return null;
        }
        if(rwType==RewardType.AFK_TIME && !Config.getInstance().enableAfkSystem()) {
            //AFK-Time Reward has been found but the AFK-System is disabled. Warn Console.
            PluginMaster.sendConsoleMessage(Level.WARNING, "Found an AFK-Time Reward '"+name+"', but the AFK-System is disabled from the Config!");
        }
        String repeatStr = cs.getString("Repeating", "");
        boolean repeat;
        try {
            repeat = Boolean.parseBoolean(repeatStr);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Repeat-Value '"+repeatStr+"' in Reward '"+name+"'");
            return null;
        }
        String grantStr = cs.getString("GrantFirst", "false");
        boolean grantFirst;
        try {
            grantFirst = Boolean.parseBoolean(grantStr);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown GrantFirst-Value '"+grantStr+"' in Reward '"+name+"'");
            return null;
        }
        String timeStr = cs.getString("Time", "0s");
        long timeMs = Utils.convertTimeStringToMS(timeStr);
        if(timeMs<=0) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Time-Value '"+timeStr+"' in Reward '"+name+"'");
            return null;
        }
        Reward rw = new Reward(name, rwType, timeMs, repeat, grantFirst);
        //Optionals
        String[] consoleCommands = cs.getStringList("ConsoleCommands").toArray(new String[0]);
        boolean foundBeginningSlash = false;
        for(int i=0;i< consoleCommands.length;i++) {
            if(consoleCommands[i].startsWith("/")) {
                foundBeginningSlash = true;
                consoleCommands[i] = consoleCommands[i].replaceAll("^/+", "");
            }
        }
        if(foundBeginningSlash) PluginMaster.sendConsoleMessage(Level.WARNING, "Found a preceding '/' in your Commands for the Reward '" +
                rw.getName() + "'.\nMake sure that this doesn't happen!");
        rw.setConsoleCommands(consoleCommands);
        String[] playerMessages = cs.getStringList("Display.PlayerMessages").toArray(new String[0]);
        rw.setPlayerMessages(playerMessages);
        String[] globalMessages = cs.getStringList("Display.GlobalMessages").toArray(new String[0]);
        rw.setGlobalMessages(globalMessages);
        String displayName = cs.getString("DisplayName");
        if(displayName!=null) rw.setDisplayName(displayName);
        String actionBarMessage = cs.getString("Display.ActionbarMessage");
        if(actionBarMessage!=null) rw.setActionBarMessage(actionBarMessage);

        List<RewardParticle> particles = new ArrayList<>();
        for(String str:cs.getStringList("Display.Particles")) {
            RewardParticle rp = createParticleFromString(str, name);
            if(rp!=null) particles.add(rp);
        }
        rw.setParticles(particles.toArray(new RewardParticle[0]));

        List<RewardSound> sounds = new ArrayList<>();
        for(String str:cs.getStringList("Display.Sounds")) {
            RewardSound rs = createSoundFromString(str, name);
            if(rs!=null) sounds.add(rs);
        }
        rw.setSounds(sounds.toArray(new RewardSound[0]));

        rw.setNeededPermission(cs.getString("PermissionNeeded", ""));

        return rw;
    }

    @Nullable
    private static  RewardParticle createParticleFromString(@NotNull String str, String rewardName) {
        String[] splits = str.replace(" ", "").split(",");
        if(splits.length<6) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Missing Parameter ('"+str+"') on a Particle for Reward '"+rewardName+"'");
            return null;
        }
        Particle part;
        try {
            part = Particle.valueOf(splits[0]);
        }catch(IllegalArgumentException e){
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Particle ('"+splits[0]+"') on a Particle for Reward '"+rewardName+"'");
            return null;
        }
        int amount;
        float offsetX, offsetY, offsetZ, extra;
        try {
            amount = Integer.parseInt(splits[1]);
            offsetX = Float.parseFloat(splits[2]);
            offsetY = Float.parseFloat(splits[3]);
            offsetZ = Float.parseFloat(splits[4]);
            extra = Float.parseFloat(splits[5]);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't parse all Numbers on a Particle for Reward '"+rewardName+"'");
            return null;
        }
        return new RewardParticle(part, amount, offsetX, offsetY, offsetZ, extra);
    }

    @Nullable
    private static RewardSound createSoundFromString(@NotNull String str, String rewardName) {
        String[] splits = str.replace(" ", "").split(",");
        if(splits.length<4) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Missing Parameter ('"+str+"') on a Particle for Reward '"+rewardName+"'");
            return null;
        }
        Sound sound;
        try {
            sound = Sound.valueOf(splits[0]);
        }catch(IllegalArgumentException e){
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Sound ('"+splits[0]+"') on a Sound for Reward '"+rewardName+"'");
            return null;
        }
        SoundCategory sc;
        try {
            sc = SoundCategory.valueOf(splits[1]);
        }catch(IllegalArgumentException e){
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown SoundCategory ('"+splits[1]+"') on a Sound for Reward '"+rewardName+"'");
            return null;
        }

        float volume, pitch;
        try {
            volume = Float.parseFloat(splits[2]);
            pitch = Float.parseFloat(splits[3]);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't parse all Numbers on a Sound for Reward '"+rewardName+"'");
            return null;
        }
        return new RewardSound(sound, sc, volume, pitch);
    }



    private boolean isInEdit;

    private Reward(String name, RewardType type, long timeMs, boolean isRepeating, boolean grantFirst) {
        super(name, type, timeMs, isRepeating, grantFirst);
    }

    protected void setEditStatus(boolean inEdit){
        this.isInEdit = inEdit;
    }

    public boolean needsPermission() {
        return !permissionNeeded.equals("");
    }

    public boolean isInEdit(){
        return isInEdit;
    }

    public void grantRewardToPlayer(Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = p.getLocation();
                if(particles!=null) {
                    for(RewardParticle part:particles) {
                        part.spawnAtLocation(loc);
                    }
                }
                if(sounds!=null) {
                    for(RewardSound rs:sounds) {
                        rs.playSound(p);
                    }
                }
                if(consoleCommands!=null){
                    for(String c:consoleCommands) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fillInPlaceholders(c, p));
                    }
                }
                if(playerMessages!=null){
                    for(String s:playerMessages) {
                        p.sendMessage(fillInPlaceholders(s, p));
                    }
                }
                if(globalMessages!=null){
                    for(String s:globalMessages) {
                        Bukkit.broadcastMessage(fillInPlaceholders(s, p));
                    }
                }
                if(actionBarMessage!=null){
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(fillInPlaceholders(actionBarMessage, p)));
                }
            }
        }.runTask(PluginMaster.getInstance());
    }

    @NotNull
    private String fillInPlaceholders(String in, @NotNull Player p) {
        String rVal = in;
        rVal = rVal.replace("<PLAYER>", p.getName());
        rVal = rVal.replace("<PLAYERDISPLAY>", p.getDisplayName());
        rVal = rVal.replace("<REWARD>", displayName);
        rVal = Lang.translateAdvancedColors(ChatColor.translateAlternateColorCodes(Config.getInstance().getColorCode(), rVal));
        return rVal;
    }

}
