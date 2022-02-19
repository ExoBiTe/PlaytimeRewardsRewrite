package com.github.exobite.mc.playtimerewards.utils;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static JavaPlugin main;

    public static void registerUtils(JavaPlugin mainInstance) {
        if(main != null) return;
        main = mainInstance;
        //setStatisticName();
    }

    public static void fillDefaultFile(String filePath) {
        if(main==null) return;
        File f = new File(main.getDataFolder()+File.separator+filePath);
        if(!f.exists()) {
            main.saveResource(filePath, true);
        }
        InputStream is = getResource(filePath);
        if(is==null) {
            System.err.println("Couldn´t find "+filePath+" in project files.");
            return;
        }
        InputStreamReader rd = new InputStreamReader(is);
        FileConfiguration fcDefault = YamlConfiguration.loadConfiguration(rd);
        boolean change = false;
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        for(String key:fcDefault.getKeys(true)) {
            if(!fc.contains(key)) {
                System.err.println("Couldn´t find "+key+" in the "+filePath+" file.");
                change = true;
                fc.set(key, fcDefault.getString(key, "DEFAULT_MESSAGE_NOT_FOUND"));

                //Debug
				/*for(String k:fcDefault.getKeys(true)) {
					System.out.println(k+" is "+fcDefault.get(k));
				}*/
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

    private static void setStatisticName(){
        String[] newestV =  {"1.13", "1.14", "1.15", "1.16", "1.17", "1.18"};
        String[] oldStatName = {"1.9", "1.10", "1.11", "1.12"};
        System.out.println(main.getServer().getVersion());
    }

    public static long getPlaytimeInTicks(UUID id){
        return 0L;
    }

    public  static long getPlaytimeInMS(Player p){
        return p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 * 1000;
    }

    public static String testRegex(String regex, String msg){
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(msg);
        return m.group();
    }

    public static long convertTimeStringToMS(String s) {
        int Days = 0, Hours = 0, Minutes = 0, Seconds = 0;

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
        long rVal = Days * 86400000 + Hours * 3600000 + Minutes * 60000 + Seconds * 1000;
        return rVal;
    }

    public static String convertTimeMsToString(long ms){
        long Days = 0, Hours = 0, Minutes = 0, Seconds = 0, Ms = 0;
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

    public static String formatTimeMsToString(long ms, String format){
        /*
        caseIgnored "%ms" -> Milliseconds
        caseIgnored "%s" -> Seconds
        caseIgnored "%m" -> Minutes
        caseIgnored "%h" -> Hours
        caseIgnored "%d" -> Days
        */
        long Days = 0, Hours = 0, Minutes = 0, Seconds = 0, Ms = 0;
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

    private static String replaceIfGreater(String in, String placeholder, long val, long greaterThan, String unit) {
        String out;
        if(val > greaterThan){
            out = in.replaceAll(placeholder, String.valueOf(val) + unit);
        }else{
            out = in.replaceAll(placeholder, "");
        }
        return out;
    }



}
