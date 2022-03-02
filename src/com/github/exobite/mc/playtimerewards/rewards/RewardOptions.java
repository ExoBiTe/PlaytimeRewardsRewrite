package com.github.exobite.mc.playtimerewards.rewards;

public class RewardOptions {

    protected final String name;
    protected String displayName;
    protected boolean isRepeating, grantFirst;
    protected long timeMs;
    protected RewardType type;

    protected String[] consoleCommands;
    protected String[] playerMessages;
    protected String[] globalMessages;
    protected RewardParticle[] particles;
    protected RewardSound[] sounds;
    protected String permissionNeeded;

    protected RewardOptions(String name, RewardType type, long timeMs, boolean isRepeating, boolean grantFirst) {
        this.name = name;
        this.type = type;
        this.timeMs = timeMs;
        this.isRepeating = isRepeating;
        this.grantFirst = grantFirst;
    }

    public String getName(){
        return name;
    }

    public RewardType getType(){
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public boolean grantFirst() {
        return grantFirst;
    }

    public void setGrantFirst(boolean grantFirst) {
        this.grantFirst = grantFirst;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public String[] getConsoleCommands() {
        return consoleCommands;
    }

    public void setConsoleCommands(String[] consoleCommands) {
        this.consoleCommands = consoleCommands;
    }

    public String[] getPlayerMessages() {
        return playerMessages;
    }

    public void setPlayerMessages(String[] playerMessages) {
        this.playerMessages = playerMessages;
    }

    public String[] getGlobalMessages() {
        return globalMessages;
    }

    public void setGlobalMessages(String[] globalMessages) {
        this.globalMessages = globalMessages;
    }

    public RewardParticle[] getParticles() {
        return particles;
    }

    public void setParticles(RewardParticle[] particles) {
        this.particles = particles;
    }

    public RewardSound[] getSounds() {
        return sounds;
    }

    public void setSounds(RewardSound[] sounds) {
        this.sounds = sounds;
    }

    public String getPermissionNeeded() {
        return permissionNeeded;
    }

    public void setNeededPermission(String permissionNeeded) {
        this.permissionNeeded = permissionNeeded;
    }

}
