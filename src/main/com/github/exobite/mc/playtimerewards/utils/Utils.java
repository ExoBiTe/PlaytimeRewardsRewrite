package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PlayerManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static PluginMaster main;

    public static void registerUtils(PluginMaster mainInstance) {
        if(main != null) return;
        main = mainInstance;
    }

    private Utils() {}

    public static boolean updateFileVersionDependent(String filename) {
        if(VersionHelper.isEqualOrLarger(main.getBukkitVersion(), new Version(1, 18, 0))) {
            return updateConfigFileWithComments(filename);
        }else{
            boolean changed = fillDefaultFile(filename);
            if(changed) PluginMaster.sendConsoleMessage(Level.INFO, "The Comments from the File "+filename+" may have been deleted.\n" +
                    "Consider using a newer Bukkit Version (1.18+) to prevent this issue.");
            return changed;
        }
    }

    public static boolean updateConfigFileWithComments(String filename) {
        File f = new File(main.getDataFolder()+File.separator+filename);
        boolean changedFile = false;
        if(!f.exists()) {
            main.saveResource(filename, true);
            changedFile = true;
        }else{
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
            YamlConfiguration defaultConf = getDefaultConfiguration(filename);
            //Iterate through all visible keys
            for(String key:defaultConf.getKeys(true)) {
                if(conf.get(key)==null) {
                    conf.set(key, defaultConf.get(key));
                    conf.setComments(key, defaultConf.getComments(key));
                    conf.setInlineComments(key, defaultConf.getInlineComments(key));
                    changedFile = true;
                }
            }
            if(changedFile) {
                try {
                    conf.save(f);
                } catch (IOException e) {
                    PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't update the File "+filename+"!");
                    e.printStackTrace();
                }
            }
        }
        return changedFile;
    }

    private static YamlConfiguration getDefaultConfiguration(String filename) {
        InputStream is = main.getResource(filename);
        if(is==null) {
            //Is handled with a runnable, as it is unknown in which Thread we are.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().disablePlugin(main);
                }
            }.runTask(main);
            throw new IllegalArgumentException("Embedded File "+filename+" not found!\nIs the Jar Modified?");
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(is));
    }

    private static boolean fillDefaultFile(String filePath) {
        if(main==null) return false;
        File f = new File(main.getDataFolder()+File.separator+filePath);
        boolean change = false;
        if(!f.exists()) {
            main.saveResource(filePath, true);
            return true;
        }
        InputStream is = getResource(filePath);
        if(is==null) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn´t find "+filePath+" in project files.");
            return false;
        }
        InputStreamReader rd = new InputStreamReader(is);
        FileConfiguration fcDefault = YamlConfiguration.loadConfiguration(rd);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        for(String key:fcDefault.getKeys(true)) {
            if(!fc.contains(key)) {
                change = true;
                fc.set(key, fcDefault.get(key));
            }
        }
        if(change) {
            //Save Fileconfig to file
            try {
                fc.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return change;
    }

    public static InputStream getResource(String filename) {
        if (main == null) {
            throw new IllegalArgumentException("Main cannot be null");
        }
        return main.getResource(filename);
    }

    public static void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        //Checks if main is != null
        if(main==null) {
            throw new IllegalArgumentException("Main cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");
        }

        File outFile = new File(main.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(main.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        //File writing
        try(OutputStream out = new FileOutputStream(outFile)) {
            if (!outFile.exists() || replace) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
            } else {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile);
            ex.printStackTrace();
        }
    }

    private static final Pattern dayPattern = Pattern.compile("\\d+[Dd]");
    private static final Pattern hourPattern = Pattern.compile("\\d+[Hh]");
    private static final Pattern minutePattern = Pattern.compile("\\d+[Mm]");
    private static final Pattern secondPattern = Pattern.compile("\\d+[Ss]");

    public static long convertTimeStringToMS(String s) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        Matcher m = dayPattern.matcher(s);
        if(m.find()){
            days = Integer.parseInt(m.group().replaceAll("[Dd]", ""));
            s = s.replace(m.group(), "");
        }
        m = hourPattern.matcher(s);
        if(m.find()){
            hours = Integer.parseInt(m.group().replaceAll("[Hh]", ""));
            s = s.replace(m.group(), "");
        }
        m = minutePattern.matcher(s);
        if(m.find()){
            minutes = Integer.parseInt(m.group().replaceAll("[Mm]", ""));
            s = s.replace(m.group(), "");
        }
        m = secondPattern.matcher(s);
        if(m.find()){
            seconds = Integer.parseInt(m.group().replaceAll("[Ss]", ""));
        }

        //String rVal = "Months: "+Months+"\nDays"+Days+"\n"+"Hours:"+Hours+"\n"+"Minutes:"+Minutes+"\nSeconds:"+
        return days * 86400000 + hours * 3600000 + minutes * 60000 + seconds * 1000;
    }

    public static String convertTimeMsToString(long ms){
        long days;
        long hours;
        long minutes;
        long seconds;
        long millis;
        days = ms / 86400000;
        long calcStep = ms % 86400000;
        hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        seconds = calcStep / 1000;
        calcStep = calcStep % 1000;
        millis = calcStep;
        return "Days: "+days+"\nHours: "+hours+"\nMinutes: "+minutes+"\nSeconds: "+seconds+"\nMillis: "+millis;
    }

    public static long[] convertTimeMsToLongs(long ms){
        long days;
        long hours;
        long minutes;
        long seconds;
        days = ms / 86400000;
        long calcStep = ms % 86400000;
        hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        seconds = calcStep / 1000;
        return new long[]{days, hours, minutes, seconds};
    }

    @NotNull
    public static String formatTimeMsToString(long ms, String format){
        /*
        caseIgnored "%ms" -> Milliseconds
        caseIgnored "%s" -> Seconds
        caseIgnored "%m" -> Minutes
        caseIgnored "%h" -> Hours
        caseIgnored "%d" -> Days
        */
        long days;
        long hours;
        long minutes;
        long seconds;
        long millis;
        days = ms / 86400000;
        long calcStep = ms % 86400000;
        hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        seconds = calcStep / 1000;
        calcStep = calcStep % 1000;
        millis = calcStep;
        String dat = format;

        dat = replaceIfGreater(dat, "%ms", millis, 0, "ms");
        dat = replaceIfGreater(dat, "%s", seconds, 0, "s");
        dat = replaceIfGreater(dat, "%m", minutes, 0," m");
        dat = replaceIfGreater(dat, "%h", hours, 0, "h");
        dat = replaceIfGreater(dat, "%d", days, 0, "d");
        //Remove unnecessary whitespaces
        dat = dat.replaceAll("( +)", "").trim();

        return dat;
    }

    @NotNull
    private static String replaceIfGreater(String in, String placeholder, long val, long greaterThan, String unit) {
        String out;
        if(val > greaterThan){
            out = in.replaceAll(placeholder, val + unit);
        }else{
            out = in.replaceAll(placeholder, "");
        }
        return out;
    }

    public static int countMatches(@NotNull String toSearch, String match) {
        //Example: "abc.abc.abc.def", "def"
        // length = 15, newlength = 12, diff 3 division by lenght of match = 1
        return (toSearch.length() - toSearch.replace(match, "").length()) / match.length();
    }

    public static void setPlaytimeToTimeMs(@NotNull Player p, long newPlaytime) {
        long diffInTicks = newPlaytime / 50;
        if(diffInTicks>Integer.MAX_VALUE) {
            //In case big differences are found
            int multiplied = Math.toIntExact(diffInTicks / Integer.MAX_VALUE);
            int rest = Math.toIntExact(diffInTicks % Integer.MAX_VALUE);
            for(int i=0;i<multiplied;i++) {
                decreasePlaytime(p, Integer.MAX_VALUE);
            }
            decreasePlaytime(p, rest);
        }else{
            decreasePlaytime(p, Math.toIntExact(diffInTicks));
        }
    }

    private static void decreasePlaytime(@NotNull Player p, int ticks) {
        int newticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE) - ticks;
        if(newticks<0) newticks = 0;
        p.setStatistic(Statistic.PLAY_ONE_MINUTE, newticks);
    }



}
