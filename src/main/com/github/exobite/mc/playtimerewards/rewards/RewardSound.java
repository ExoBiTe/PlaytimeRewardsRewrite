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
        p.playSound(p.getLocation(), s, sc, vol, pitch);
        //ReflectionHelper.getInstance().callPlaySound(this, p);
    }

    public Sound getSound() {
        return s;
    }

    public SoundCategory getSoundCategory() {
        return sc;
    }

    public float getVolume(){
        return vol;
    }

    public float getPitch(){
        return pitch;
    }

    public String toString() {
        return s.toString() + ", " + sc.toString() + ", " + vol + ", " + pitch;
    }

}
