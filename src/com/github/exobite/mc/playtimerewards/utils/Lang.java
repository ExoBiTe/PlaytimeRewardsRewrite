package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Lang {

    private static Map<String, String> Messages = new HashMap<>();

    public static boolean exists(String msg) {
        return Messages.containsKey(msg);
    }

    public static void debugTesting(){
        PluginMaster.sendConsoleMessage(Level.WARNING, "It Works!!!");
    }



    public static String getMessageWithArgs(String msg, String ... args) {

        return "";
    }



}
