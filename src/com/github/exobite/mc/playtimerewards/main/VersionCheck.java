package com.github.exobite.mc.playtimerewards.main;

import org.bukkit.plugin.java.JavaPlugin;

public class VersionCheck {

    private static int major = 0;
    private static int minor = 0;
    private static int patch = 0;

    public static VER_RESULT canRun(JavaPlugin main){
        if(major == 0) {
            parseVersion(main);
        }

        for (VER_RESULT vr : VER_RESULT.values()) {
            if(meetsRequirements(vr)){
                return vr;
            }
        }
        return VER_RESULT.UNKNOWN_VERSION;

    }

    private static boolean meetsRequirements(VER_RESULT vr) {
        return vr.getMinMajor() >= major && vr.getMinMinor() >= minor && vr.getMinPatch() >= patch;
    }

    public static void parseVersion(JavaPlugin main) {
        String v = main.getServer().getBukkitVersion();
        //System.out.println(v);
        String[] garbageSplit = v.split("-");
        if(garbageSplit.length < 1) return;     //Error
        String[] versionNumbers = garbageSplit[0].split(".");
        if(versionNumbers.length < 2) return;   //Error
        major = Integer.valueOf(versionNumbers[0]);
        minor = Integer.valueOf(versionNumbers[1]);
        if(versionNumbers.length>=3) patch = Integer.valueOf(versionNumbers[2]);
    }

    public static int getMajor(){
        return major;
    }

    public static int getMinor(){
        return minor;
    }

    public static int getPatch(){
        return patch;
    }

}
