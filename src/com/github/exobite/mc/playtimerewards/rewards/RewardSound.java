package com.github.exobite.mc.playtimerewards.rewards;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class RewardSound {

    private final Sound s;
    private final SoundCategory sc;
    private final float vol, pitch;

    public RewardSound(Sound s, SoundCategory sc, float vol, float pitch) {
        this.s = s;
        this.sc = sc;
        this.vol = vol;
        this.pitch = pitch;
    }

    public void playSound(Player p) {
        p.playSound(p, s, sc, vol, pitch);
    }

}
