package com.github.exobite.mc.playtimerewards.rewards;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class Reward {

    private RewardParticle[] particles;
    private RewardSound[] sounds;

    public Reward() {

    }

    public void grantRewardToPlayer(Player p) {
        //Mlem
        Location loc = p.getLocation();
        for(RewardParticle part:particles) {
            part.spawnAtLocation(loc);
        }
        for(RewardSound rs:sounds) {
            rs.playSound(p);
        }
    }


    public void dummy() {

    }

}
