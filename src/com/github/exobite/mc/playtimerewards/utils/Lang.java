package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.ChannelNameTooLongException;
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

    private final Map<String, MessageData> Messages = new HashMap<>();
    private final JavaPlugin main;

    private Lang(JavaPlugin main){
        this.main = main;
        File f = new File(main.getDataFolder()+File.separator+"lang.yml");
        if(!f.exists()) {
            createLangFile(f);
        }else{
            //Only read File if it already exists
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
            for(String key:conf.getKeys(false)){
                Messages.put(key, createMessageData(conf.getString(key, "ERR_NO_MESSAGE_FOUND__"+key)));
            }
            //TODO: Compare if Messages read from File are containing all needed Messages, add missing Messages to the File.
        }
    }

    private void createLangFile(File f){
        Messages.put("PTR_SUC_PLAYTIMECOMMAND", createMessageData("§aYour Playtime is %[0]d %[1]h %[2]m %[3]s\nYour Sessiontime is %[4]d %[5]h %[6]m %[7]s"));
        Messages.put("CMD_ERR_NO_PERMISSION", createMessageData("§4You don't have the Permission to do this."));

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

    private MessageData createMessageData(String msg) {
        if(msg==null) return new MessageData("ERR_NO_MESSAGE_FOUND_", 0);
        String replaced = msg.replaceAll("%\\[[0-9]\\]", "%[#]");
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
            rVal = rVal.replace("%["+i+"]", args[i]);
        }
        rVal = ChatColor.translateAlternateColorCodes(PluginMaster.getColorCode(), rVal);
        return rVal;
    }




}
