package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.rewards.RewardSound;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.io.Reader;
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
    private Method gsonParserMethod;
    private final boolean use1_18Methods;

    private ReflectionHelper(){
        //Check for the Version
        use1_18Methods = VersionHelper.isEqualOrLarger(PluginMaster.getInstance().getBukkitVersion(), new Version(1, 18, 0));

        //Cache the Methods for later use
        //setPlaySoundMethod();
        setParseReaderMethod();
    }

    /*
    Don't use Reflection for Playing Sounds anymore
    - tbh i can't really remember why i did this in the first place?

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
    */

    private void setParseReaderMethod() {
        Class<JsonParser> clazz = JsonParser.class;
        try {
            if(use1_18Methods) {
                gsonParserMethod = clazz.getMethod("parseReader", Reader.class);
            }else{
                gsonParserMethod = clazz.getMethod("parse", Reader.class);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public JsonElement parseReader(InputStreamReader isr) {
        if(gsonParserMethod==null) return null;
        JsonElement rVal = null;
        try {
            gsonParserMethod.setAccessible(true);
            if(use1_18Methods) {
                rVal = (JsonElement) gsonParserMethod.invoke(null, isr);
            }else{
                rVal = (JsonElement) gsonParserMethod.invoke(new JsonParser(), isr);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return rVal;
    }


}
