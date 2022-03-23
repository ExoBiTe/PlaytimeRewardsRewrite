package com.github.exobite.mc.playtimerewards.rewards;

import org.bukkit.Location;
import org.bukkit.Particle;

public class RewardParticle {

    private final Particle p;
    private final int amount;
    private final float offsetX, offsetY, offsetZ, extra;

    public RewardParticle(Particle p, int amount, float offsetX, float offsetY, float offsetZ, float extra) {
        this.p = p;
        this.amount = amount;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.extra = extra;
    }

    public void spawnAtLocation(Location loc) {
        if(loc.getWorld()==null) return;
        loc.getWorld().spawnParticle(p, loc, amount, offsetX, offsetY, offsetZ, extra);
    }

    public String toString() {
        return p.toString() + ", " + amount + ", " + offsetX + ", " + offsetY + ", " + offsetZ + ", " + extra;
    }

}
