package com.github.exobite.mc.playtimerewards.utils;

import org.bukkit.Bukkit;

public class OLDVersionIdentifier {

    private static OLDVersionIdentifier instance;

    public static OLDVersionIdentifier getInstance() {
        if(instance==null){
            instance = new OLDVersionIdentifier();
        }
        return instance;
    }

    private int major = 0,
            minor = 0,
            patch = 0;

    private OLDVersionIdentifier() {
        String v = Bukkit.getServer().getBukkitVersion();
        String[] garbageSplit = v.split("-");
        if(garbageSplit.length < 1) return;     //Error
        String[] versionNumbers = garbageSplit[0].split("\\.");
        if(versionNumbers.length < 2) return;   //Error
        major = Integer.parseInt(versionNumbers[0]);
        minor = Integer.parseInt(versionNumbers[1]);
        if(versionNumbers.length>=3) patch = Integer.parseInt(versionNumbers[2]);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }


    public boolean isEqual(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
        return major1==major2 && minor1==minor2 && patch1==patch2;
    }

    public boolean isGreater(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
        return (major1==major2 && minor1==minor2 && patch1>patch2) ||
                (major1==major2 && minor1>minor2) ||
                (major1>major2);
    }

    public boolean isSmaller(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
        return (major1==major2 && minor1==minor2 && patch1<patch2) ||
                (major1==major2 && minor1<minor2) ||
                (major1<major2);
    }

    public boolean isEqualOrGreater(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
        return isGreater(major1, minor1, patch1, major2, minor2, patch2) || isEqual(major1, minor1, patch1, major2, minor2, patch2);
    }

    public boolean isEqualOrSmaller(int major1, int minor1, int patch1, int major2, int minor2, int patch2) {
        return isSmaller(major1, minor1, patch1, major2, minor2, patch2) || isEqual(major1, minor1, patch1, major2, minor2, patch2);
    }

    public boolean isEqual(int major, int minor, int patch) {
        return isEqual(this.major, this.minor, this.patch, major, minor, patch);
    }

    public boolean isGreater(int major, int minor, int patch) {
        return isGreater(this.major, this.minor, this.patch, major, minor, patch);
    }

    public boolean isSmaller(int major, int minor, int patch) {
        return isSmaller(this.major, this.minor, this.patch, major, minor, patch);
    }

    public boolean isEqualOrGreater(int major, int minor, int patch) {
        return isEqualOrGreater(this.major, this.minor, this.patch, major, minor, patch);
    }

    public boolean isEqualOrSmaller(int major, int minor, int patch){
        return isEqualOrSmaller(this.major, this.minor, this.patch, major, minor, patch);
    }


}
