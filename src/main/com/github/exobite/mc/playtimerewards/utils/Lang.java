package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lang {

    private static Lang instance;

    public static Lang registerLangManager(JavaPlugin main){
        if(instance==null) {
            instance = new Lang(main);
        }
        return instance;
    }

    public static Lang getInstance() {
        return instance;
    }

    public static void reloadLang(){
        instance = new Lang(instance.main);
    }

    private final JavaPlugin main;

    private Lang(@NotNull JavaPlugin main){
        this.main = main;
        File langFile = new File(main.getDataFolder()+File.separator+"lang.yml");

        Msg.setPapiIsRegistered(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
        if(!langFile.exists()) {
            createNewLangFile(langFile);
        }else{
            readMessagesFromFile(langFile);
        }
    }

    private record UpdateConfigResponse(boolean hasChanged, YamlConfiguration conf) { }

    private void createNewLangFile(File f) {
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        boolean setComments = VersionHelper.isEqualOrLarger(VersionHelper.getBukkitVersion(), new Version(1, 18, 0));
        for(Msg msg:Msg.values()) {
            if(msg.showInFile()){
                conf.set(msg.toString(), msg.getMessage());
                if(setComments) conf.setComments(msg.toString(), stringToList(msg.getComment()));
            }
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PluginMaster.sendConsoleMessage(Level.INFO, "A new lang.yml File has been created!");
    }

    private void readMessagesFromFile(File f) {
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        for(String key:conf.getKeys(true)) {
            try {
                //Overwrite all found Messages
                Msg.valueOf(key).setMessage(conf.getString(key));
            }catch(IllegalArgumentException e) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Unknown Message '"+key+"' found in lang.yml!");
            }
        }

        //After getting all values, try to update the File
        UpdateConfigResponse response = updateConfiguration(conf);
        if(response.hasChanged) {
            PluginMaster.sendConsoleMessage(Level.INFO, "Your lang.yml File has been updated!");
            try {
                response.conf.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private UpdateConfigResponse updateConfiguration(YamlConfiguration conf) {
        //With Comments is only 1.18+
        if(VersionHelper.isEqualOrLarger(VersionHelper.getBukkitVersion(), new Version(1, 18, 0))) {
            return updateConfigurationWithComments(conf);
        }else{
            return updateConfigurationWithoutComments(conf);
        }
    }

    private UpdateConfigResponse updateConfigurationWithoutComments(YamlConfiguration conf) {
        boolean changed = false;
        for(Msg m:Msg.values()) {
            if(m.showInFile() && conf.get(m.toString())==null) {
                changed = true;
                conf.set(m.toString(), m.getMessage());
            }
        }
        return new UpdateConfigResponse(changed, conf);
    }

    private UpdateConfigResponse updateConfigurationWithComments(YamlConfiguration conf) {
        YamlConfiguration newConf = new YamlConfiguration();
        List<String> copiedKeys = new ArrayList<>();
        boolean changed = false;
        //First copy all known and visible in file Messages into a new Config
        for(Msg m:Msg.values()) {
            boolean exists = conf.get(m.toString())!=null;
            if(m.showInFile() || exists) {
                List<String> comments = new ArrayList<>();
                if(!exists) {
                    changed = true;
                }else{
                    //Try to get old comments
                    comments = conf.getComments(m.toString());
                }
                newConf.set(m.toString(), m.getMessage());
                if(comments.isEmpty()) {
                    comments = stringToList(m.getComment());
                }
                newConf.setComments(m.toString(), comments);
                copiedKeys.add(m.toString());
            }
        }
        boolean first = true;
        for(String key:conf.getKeys(true)) {
            if(copiedKeys.contains(key)) continue;
            newConf.set(key, conf.getString(key));
            newConf.setComments(key, conf.getComments(key));
            if(first){
                newConf.setComments(key, stringToList("Unknown Messages:"));
                first = false;
            }
        }
        return new UpdateConfigResponse(changed, newConf);
    }

    private List<String> stringToList(String s) {
        List<String> l = new ArrayList<>();
        l.add(null);
        if(s.contains("\n")) {
            String[] split = s.split("\n");
            l.addAll(Arrays.asList(split));
        }else{
            l.add(s);
        }
        return l;
    }

    public String getMessage(@NotNull Msg msg, @Nullable String ... args) {
        return getMessage(null, msg, args);
    }

    public String getMessage(Player p, @NotNull Msg msg, @Nullable String ... args) {
        int neededAmount = msg.getArgAmount();
        if(neededAmount > 0) {
            int givenAmount = args==null ? 0 : args.length;
            if(givenAmount < neededAmount) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Too few Parameters given for Message '"+msg+"': Given "+givenAmount+", expected "+neededAmount+"!");
                return "ERR_TOO_FEW_ARGS__"+msg;
            }else if(givenAmount > neededAmount) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Too many Parameters given for Message '"+msg+"': Given "+givenAmount+", expected "+neededAmount+"!");
            }
        }
        String rVal = msg.getMessage();
        for(int i=0;i<neededAmount;i++) {
            if(args[i]==null) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Message was given null as Parameter for Message '"+msg+"' as param no. "+i);
                return "ERR_NULL_PASSED__"+msg;
            }
            rVal = rVal.replace("%["+i+"]", args[i]);
        }
        if(msg.usesPapi() && p != null) {
            rVal = PlaceholderAPI.setPlaceholders(p, rVal);
        }
        rVal = ChatColor.translateAlternateColorCodes(Config.getInstance().getColorCode(), translateAdvancedColors(rVal));
        return rVal;
    }

    protected void reloadAsync(){
        new BukkitRunnable(){
            @Override
            public void run() {
                //Create new instance of Lang, and get rid of old instance
                instance = new Lang(main);
                PluginMaster.sendConsoleMessage(Level.INFO, "Reloaded all Messages from File.");
            }
        }.runTaskAsynchronously(main);
    }

    //Copied from MinecraftMaker Message.java
    private static final Pattern hexColorFollowing = Pattern.compile("§\\([A-Fa-f0-9]{6}\\)");
    private static final Pattern hexColorArea = Pattern.compile("§\\([a-fA-F0-9]{6}-\\)[\\s\\S]*?§\\(-[a-fA-F0-9]{6}\\)");

    //Example: §(FFFFFF) -> Colors the following Chars in the specified Hex-Color
    //Example: §(FFFFFF-) Some cool text §(-FFAA00) -> Colors the Chars in between the two specifiers &(*-) and &(-*)
    public static String translateAdvancedColors(String in) {
        String outString = in;
        Matcher m1 = hexColorArea.matcher(in);
        while(m1.find()) {
            String group = m1.group();
            String hx1 = group.substring(2, 8);
            String hx2 = group.substring(group.length()-7, group.length()-1);
            String stripped = group.substring(10, group.length()-10);
            int strlen = group.length() - 20;
            int lenWithoutWhitespaces = stripped.replace(" ", "").length();
            int r = Integer.parseInt(hx1.substring(0, 2), 16);
            int rIncr = (Integer.parseInt(hx2.substring(0, 2), 16) - r) / lenWithoutWhitespaces;
            int g = Integer.parseInt(hx1.substring(2, 4), 16);
            int gIncr = (Integer.parseInt(hx2.substring(2, 4), 16) - g) / lenWithoutWhitespaces;
            int b = Integer.parseInt(hx1.substring(4), 16);
            int bIncr = (Integer.parseInt(hx2.substring(4), 16) - b) / lenWithoutWhitespaces;
            StringBuilder sb = new StringBuilder();
            String[] splits = outString.split(Pattern.quote(group));
            if(splits.length>0) sb.append(splits[0]);
            int counter = 0;
            for(int i=0;i<strlen;i++) {
                if(counter>=strlen) break;
                if(stripped.charAt(i)==' '){
                    sb.append(' ');
                    continue; //Skip whitespaces
                }
                String hxr = Integer.toHexString(r + (counter * rIncr));
                String hxg = Integer.toHexString(g + (counter * gIncr));
                String hxb = Integer.toHexString(b + (counter * bIncr));
                if(hxr.length()<2) hxr = "0"+hxr;
                if(hxg.length()<2) hxg = "0"+hxg;
                if(hxb.length()<2) hxb = "0"+hxb;
                sb.append(net.md_5.bungee.api.ChatColor.of("#"+hxr+hxg+hxb)).append(stripped.charAt(i));
                counter++;
            }
            //Append the stuff behind the Color-specifier back to the String
            if(splits.length>1) {
                //First Reset Chatcolor
                sb.append(net.md_5.bungee.api.ChatColor.RESET);
                for(int j=1;j<splits.length;j++) {
                    sb.append(splits[j]);
                }
            }
            outString = sb.toString();
        }
        Matcher m2 = hexColorFollowing.matcher(in);
        while(m2.find()) {
            //Get the Hex-Chars, strip the parenthesis
            String group = m2.group();
            String colors = "#" + group.substring(2, group.length()-1);
            outString = outString.replaceFirst(hexColorFollowing.toString(), net.md_5.bungee.api.ChatColor.of(colors).toString());
        }
        return outString;
    }


}
