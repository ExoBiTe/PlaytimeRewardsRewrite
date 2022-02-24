package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.rewards.RewardSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {

    private static ReflectionHelper instance;

    public static ReflectionHelper getInstance() {
        if(instance==null) {
            instance = new ReflectionHelper();
        }
        return instance;
    }

    private Method playSoundMethod;
    private final boolean use1_18Methods;

    private ReflectionHelper(){
        //Check for the Version
        use1_18Methods = VersionHelper.isEqualOrLarger(PluginMaster.getInstance().getBukkitVersion(), new Version(1, 18, 0));

        //Cache the Methods for later use
        setPlaySoundMethod();
    }

    private void setPlaySoundMethod(){
        Class<Player> bukkitPlayer = Player.class;
        try {
            if(use1_18Methods){
                //Version 1.18+
                playSoundMethod = bukkitPlayer.getMethod("playSound", Entity.class, Sound.class, SoundCategory.class, float.class, float.class);
            }else{
                //Until Version 1.17.1
                playSoundMethod = bukkitPlayer.getMethod("playSound", Location.class, Sound.class, SoundCategory.class, float.class, float.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void callPlaySound(RewardSound rw, Player p) {
        if(playSoundMethod==null) return;
        try {
            playSoundMethod.setAccessible(true);
            if(use1_18Methods){
                //Version 1.18+
                playSoundMethod.invoke(p, p, rw.getSound(), rw.getSoundCategory(), rw.getVolume(), rw.getPitch());
            }else{
                //Until Version 1.17.1
                playSoundMethod.invoke(p, p.getLocation(), rw.getSound(), rw.getSoundCategory(), rw.getVolume(), rw.getPitch());
            }
        }catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
