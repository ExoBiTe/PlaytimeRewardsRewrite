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

    private Method gsonParserMethod;
    private final boolean use1_18Methods;

    private ReflectionHelper(){
        //Check for the Version
        use1_18Methods = VersionHelper.isEqualOrLarger(PluginMaster.getInstance().getBukkitVersion(), new Version(1, 18, 0));

        //Cache the Methods for later use
        setParseReaderMethod();
    }

    @SuppressWarnings("deprecation")
    //Surpressing deprecated "new JsonParser()" as it already is only called when using a Version, where it isn't deprecated.
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
