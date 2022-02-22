package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class VersionIdentifier {

    private static VersionIdentifier instance;

    public static VersionIdentifier getInstance() {
        if(instance==null){
            instance = new VersionIdentifier();
        }
        return instance;
    }

    private int major = 0,
            minor = 0,
            patch = 0;

    private VersionIdentifier() {
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

    public boolean isEqual(int major, int minor, int patch) {
        return this.major==major && this.minor==minor && this.patch==patch;
    }

    public boolean isGreater(int major, int minor, int patch) {
        return (this.major==major && this.minor==minor && this.patch>patch) ||
                (this.major==major && this.minor>minor) ||
                (this.major>major);
    }

    public boolean isSmaller(int major, int minor, int patch) {
        return (this.major==major && this.minor==minor && this.patch<patch) ||
                (this.major==major && this.minor<minor) ||
                (this.major<major);
    }

    public boolean isEqualOrGreater(int major, int minor, int patch) {
        return isGreater(major, minor, patch) || isEqual(major, minor, patch);
    }

    public boolean isEqualOrSmaller(int major, int minor, int patch) {
        return isSmaller(major, minor, patch) || isEqual(major, minor, patch);
    }

}
