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
        for(Msg msg:Msg.values()) {
            if(msg.showInFile()){
                conf.set(msg.toString(), msg.getMessage());
                conf.setComments(msg.toString(), stringToList(msg.getComment()));
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
        //With Comments is only 1.18+, as
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
                if(comments.size()==0) {
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
        rVal = ChatColor.translateAlternateColorCodes(Config.getInstance().getColorCode(), rVal);
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

}
