package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static PluginMaster main;

    public static void registerUtils(PluginMaster mainInstance) {
        if(main != null) return;
        main = mainInstance;
    }

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
            System.err.println("Couldn´t find "+filePath+" in project files.");
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
        File outDir = new File(main.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        //File writing
        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                System.out.println("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            System.out.println("Could not save " + outFile.getName() + " to " + outFile);
            ex.printStackTrace();
        }
    }

    public static long convertTimeStringToMS(String s) {
        long Days = 0, Hours = 0, Minutes = 0, Seconds = 0;

        Pattern p = Pattern.compile("[0-9]+D");
        Matcher m = p.matcher(s);
        if(m.find()){
            Days = Integer.parseInt(m.group().replace("D", ""));
            s = s.replace(m.group(), "");
        }

        p = Pattern.compile("[0-9]+h");
        m = p.matcher(s);
        if(m.find()){
            Hours = Integer.parseInt(m.group().replace("h", ""));
            s = s.replace(m.group(), "");
        }

        p = Pattern.compile("[0-9]+m");
        m = p.matcher(s);
        if(m.find()){
            Minutes = Integer.parseInt(m.group().replace("m", ""));
            s = s.replace(m.group(), "");
        }

        p = Pattern.compile("[0-9]+s");
        m = p.matcher(s);
        if(m.find()){
            Seconds = Integer.parseInt(m.group().replace("s", ""));
            s = s.replace(m.group(), "");
        }

        //String rVal = "Months: "+Months+"\nDays"+Days+"\n"+"Hours:"+Hours+"\n"+"Minutes:"+Minutes+"\nSeconds:"+
        return Days * 86400000 + Hours * 3600000 + Minutes * 60000 + Seconds * 1000;
    }

    public static String convertTimeMsToString(long ms){
        long Days, Hours, Minutes, Seconds, Ms;
        Days = ms / 86400000;
        long calcStep = ms % 86400000;
        Hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        Minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        Seconds = calcStep / 1000;
        calcStep = calcStep % 1000;
        Ms = calcStep;
        return "Days: "+Days+"\nHours: "+Hours+"\nMinutes: "+Minutes+"\nSeconds: "+Seconds+"\nMillis: "+Ms;
    }

    public static long[] convertTimeMsToLongs(long ms){
        long Days, Hours, Minutes, Seconds;
        Days = ms / 86400000;
        long calcStep = ms % 86400000;
        Hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        Minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        Seconds = calcStep / 1000;
        return new long[]{Days, Hours, Minutes, Seconds};
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
        long Days, Hours, Minutes, Seconds, Ms;
        Days = ms / 86400000;
        long calcStep = ms % 86400000;
        Hours = calcStep / 3600000;
        calcStep = calcStep % 3600000;
        Minutes = calcStep / 60000;
        calcStep = calcStep % 60000;
        Seconds = calcStep / 1000;
        calcStep = calcStep % 1000;
        Ms = calcStep;
        String dat = format;

        dat = replaceIfGreater(dat, "%ms", Ms, 0, "Ms");
        dat = replaceIfGreater(dat, "%s", Seconds, 0, "S");
        dat = replaceIfGreater(dat, "%m", Minutes, 0," M");
        dat = replaceIfGreater(dat, "%h", Hours, 0, "H");
        dat = replaceIfGreater(dat, "%d", Days, 0, "D");
        //Remove unnecessary whitespaces
        dat = dat.replaceAll("( +)", " ").trim();

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



}
