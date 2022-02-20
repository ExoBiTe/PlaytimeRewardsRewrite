package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.module.Configuration;
import java.util.logging.Level;

public class Reward {

    protected static Reward getRewardFromYaml(ConfigurationSection cs){
        if(cs==null) return null;
        String name = cs.getName();
        //System.out.println(cs.getConfigurationSection());
        String rwTypeStr = cs.getString("CountType", "");
        RewardType rwType;
        try {
            rwType = RewardType.valueOf(rwTypeStr);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown RewardType '"+rwTypeStr+"' in Reward '"+name+"'");
            return null;
        }
        String repeatStr = cs.getString("Repeating", "");
        boolean repeat;
        try {
            repeat = Boolean.parseBoolean(repeatStr);
        }catch(IllegalArgumentException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Repeat-Value '"+repeatStr+"' in Reward '"+name+"'");
            return null;
        }
        String timeStr = cs.getString("Time", "0s");
        long timeMs = Utils.convertTimeStringToMS(timeStr);
        if(timeMs<=0) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Unknown Time-Value '"+timeStr+"' in Reward '"+name+"'");
            return null;
        }
        return new Reward(name, rwType, timeMs, repeat);
    }

    private String name;
    private String displayName;
    private boolean isRepeating;
    private long timeMs;
    private RewardType type;

    private String[] consoleCommands;
    private String[] playerMessages;
    private String[] globalMessages;
    private RewardParticle[] particles;
    private RewardSound[] sounds;

    private Reward(String name, RewardType type, long timeMs, boolean isRepeating) {
        this.name = name;
        this.type = type;
        this.timeMs = timeMs;
        this.isRepeating = isRepeating;

        //Dummy
        playerMessages = new String[]{"Hello!!!"};
    }

    private Reward setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    private Reward setConsoleCommands(String[] consoleCommands){
        this.consoleCommands = consoleCommands;
        return this;
    }

    private Reward setPlayerMessages(String[] playerMessages){
        this.playerMessages = playerMessages;
        return this;
    }

    private Reward setGlobalMessages(String[] globalMessages){
        this.globalMessages = globalMessages;
        return this;
    }

    private Reward setGlobalMessages(RewardParticle[] particles){
        this.particles = particles;
        return this;
    }

    private Reward setGlobalMessages(RewardSound[] sounds){
        this.sounds = sounds;
        return this;
    }

    public String getName() {
        return name;
    }

    public RewardType getType() {
        return type;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void grantRewardToPlayer(Player p) {
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
        if(playerMessages!=null){
            for(String s:playerMessages) {
                p.sendMessage(s);
            }
        }

    }


    public void dummy() {

    }

}
