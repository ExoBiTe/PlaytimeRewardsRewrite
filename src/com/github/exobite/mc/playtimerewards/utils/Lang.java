package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

    private record MessageData(String message,
                               int varAmount) {
    }

    private Map<String, MessageData> Messages = new HashMap<>();
    private final JavaPlugin main;
    private final File langFile;

    private Lang(JavaPlugin main){
        this.main = main;
        langFile = new File(main.getDataFolder()+File.separator+"lang.yml");
        if(!langFile.exists()) {
            Messages = getDefaultMap();
            createLangFile(langFile);
        }else{
            //Only read File if it already exists
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(langFile);
            for(String key:conf.getKeys(false)){
                Messages.put(key, createMessageData(conf.getString(key, "ERR_NO_MESSAGE_FOUND__"+key)));
            }
            //Compare loaded Messages to needed Messages
            compareAndFillMessageMap();
        }
    }

    private void createLangFile(File f){
        //Write data to File
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        for(String key:Messages.keySet()) {
            conf.set(key, Messages.get(key).message);
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compareAndFillMessageMap(){
        Map<String, MessageData> defaults = getDefaultMap();
        boolean changed = false;
        for(String s:defaults.keySet()) {
            boolean contains = false;
            for(String m:Messages.keySet()) {
                if(s.equals(m)) {
                    contains = true;
                    break;
                }
            }
            if(!contains) {
                Messages.put(s, defaults.get(s));
                changed = true;
            }
        }
        if(changed) {
            createLangFile(langFile);
            PluginMaster.sendConsoleMessage(Level.INFO, "Your lang.yml File has been updated!");
        }
    }

    private Map<String, MessageData> getDefaultMap() {
        Map<String, MessageData> data = new HashMap<>();
        data.put("CMD_SUC_PT_OWN", createMessageData("§aYour Playtime is %[0]d %[1]h %[2]m %[3]s\nYour Sessiontime is %[4]d %[5]h %[6]m %[7]s"));
        data.put("CMD_SUC_PT_OTHER", createMessageData("§6%[0]§a's Playtime is %[1]d %[2]h %[3]m %[4]s\n§6%[5]§a's Sessiontime is %[6]d %[7]h %[8]m %[9]s"));
        data.put("CMD_SUC_PT_OTHER_OFFLINE", createMessageData("§6%[0]§a's Playtime is %[1]d %[2]h %[3]m %[4]s"));

        data.put("CMD_SUC_PTTOP_HEADER", createMessageData("§7Listing the top §b%[0] §7Playtimes:"));
        data.put("CMD_SUC_PTTOP_ENTRY", createMessageData("§6%[0]§7: §b%[1]§7 - has played %[2]d %[3]h %[4]m and %[5]s"));


        data.put("CMD_ERR_TOO_MANY_REQUESTS", createMessageData("§4You can't do that right now. Try again later!"));
        data.put("CMD_ERR_NO_PERMISSION", createMessageData("§4You don't have the Permission to do this."));
        data.put("CMD_ERR_PLAYER_NOT_FOUND", createMessageData("§4Can't find the Player '§6%[0]§4'!"));

        data.put("NOTIF_UPDATE_AVAILABLE", createMessageData("§6Version %[0] of PlaytimeRewards is available (Running v%[1])!"));

        //GUI Strings (a whole bunch)
        data.put("GUI_EDIT_REWARD_WINDOWNAME", createMessageData("§6Editing Reward: §2%[0]"));
        data.put("GUI_EDIT_REWARD_EXIT_NOSAVE_NAME", createMessageData("§4Exit and discard Changes"));
        data.put("GUI_EDIT_REWARD_EXIT_NOSAVE_LORE", createMessageData("§cThis Option discards all changes\n§cyou've made and ends the editing."));
        data.put("GUI_EDIT_REWARD_EXIT_SAVE_NAME", createMessageData("§2Exit and Save Changes"));
        data.put("GUI_EDIT_REWARD_EXIT_SAVE_LORE", createMessageData("§aThis Option saves all changes\n§ayou've made and ends the editing."));



        return data;
    }

    private MessageData createMessageData(String msg) {
        if(msg==null) return new MessageData("ERR_NO_MESSAGE_FOUND_", 0);
        String replaced = msg.replaceAll("%\\[[0-9]]", "%[#]");
        int amount = StringUtils.countMatches(replaced, "%[#]");
        return new MessageData(msg, amount);
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

    public boolean exists(String msg) {
        return Messages.containsKey(msg);
    }

    public Set<String> getRegisteredMessages() {
        return Messages.keySet();
    }

    public String getRawMessage(String message){
        return Messages.containsKey(message) ? Messages.get(message).message : "ERR_NO_MESSAGE_FOUND__"+message;
    }

    public int getParamAmount(String msg) {
        if(!exists(msg)) return 0;
        return Messages.get(msg).varAmount;
    }

    public String getMessageWithArgs(String msg, String ... args) {
        if(!exists(msg)) return "ERR_NO_MESSAGE_FOUND__"+msg;
        MessageData md = Messages.get(msg);
        if(md.varAmount>0) {
            int givenAmount = args==null ? 0 : args.length;
            if(givenAmount < md.varAmount) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Too few Parameters given for Message '"+msg+"': Given "+givenAmount+", expected "+md.varAmount+"!");
                return "ERR_TOO_FEW_ARGS__"+msg;
            }else if(givenAmount > md.varAmount) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Too many Parameters given for Message '"+msg+"': Given "+givenAmount+", expected "+md.varAmount+"!");
            }
        }
        String rVal = md.message;
        for(int i=0;i<md.varAmount;i++) {
            if(args[i]==null) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Message was given null as Parameter for Message '"+msg+"' as param no. "+i);
                return "ERR_NULL_PASSED__"+msg;
            }
            rVal = rVal.replace("%["+i+"]", args[i]);
        }
        rVal = ChatColor.translateAlternateColorCodes(Config.getInstance().getColorCode(), rVal);
        return rVal;
    }




}
