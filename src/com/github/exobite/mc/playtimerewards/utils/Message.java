package com.github.exobite.mc.playtimerewards.utils;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public enum Message {

    CMD_FAIL_NO_UUID, CMD_FAIL_UNKNOWN_ID, CMD_FAIL_UNKNOWN_PLAYER,

    PTR_SUC_PLAYTIMECOMMAND,

    ERR_GUI_WRONG_ARGS, ERR_GUI_NOT_FOUND, ERR_GUI_MOD_NOT_FOUND, ERR_NO_CONSOLE_CMD,

    MSG_NO_PERMISSION,

    ;

    private static JavaPlugin main;
    private static final char colorCode = '&';
    private static final String varValue1 = "VAR[", varValue2 = "]";

    public static void registerMessages(JavaPlugin main) {
        if(Message.main!=null){
            //Already registered
            return;
        }
        Message.main = main;
        Utils.fillDefaultFile("lang.yml");
        readMessagesFromFile("lang.yml");

    }

    static void readMessagesFromFile(String filePath) {
        File f = new File(main.getDataFolder()+File.separator+filePath);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        for(Message m:Message.values()) {
            if(!fc.contains(m.toString())) continue;
            String msg = fc.getString(m.toString());
            for(int i=0;i<10;i++) {
                //Replace numbers 0-9 with %N%
                if(!msg.contains(i+"")) break;	//Break loop when number i isn´t found
                msg = msg.replace(i+"", "%N%");
            }
            int amount = StringUtils.countMatches(msg, varValue1 + "%N%" + varValue2);
            msg = fc.getString(m.toString());
            m.setData(msg, amount);
        }
    }

    private static String getMessageWithArgs(Message m, String[] args) {
        String msg = "";
        //Construct raw String
        if(msg.toString().startsWith("SYS_")) {
            msg = "["+main.getDescription().getName()+"] ";
        }
        msg = msg + ChatColor.translateAlternateColorCodes(colorCode, m.getRawMessage());
        //Check if the given args are correct
        int given = args==null ? 0 : args.length;
        if(m.getArgAmount()==given) {
            msg = fillRawStringWithArgs(msg, args);
        }else {
            System.err.print("["+main.getDescription().getName()+"] The Message "+m.toString()+" was given a wrong ArgAmount. "
                    + "Make sure it gets "+m.getArgAmount()+" Args (got "+given+" args). Sending the unfilled Message now. Thread Dump:");
            Thread.dumpStack();
        }

        return msg;
    }

    private static String fillRawStringWithArgs(String str, String[] args) {
        if(args==null) return str;
        for(int i=0;i<args.length;i++) {
            str = str.replace(varValue1+i+varValue2, args[i]);
        }
        return str;
    }

    private String message = null;
    private int argAmount = 0;

    private void setData(String message, int argAmount) {
        this.message = message;
        this.argAmount = argAmount;
    }

    public int getArgAmount() {
        return argAmount;
    }

    public String getMessage(String... args) {
        return getMessageWithArgs(this, args);
    }

    public String getRawMessage() {
        return message;
    }


}
