package com.github.exobite.mc.playtimerewards.web;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;

public class MotdReader {

    private static MotdReader instance;

    public static MotdReader getInstance() {
        return instance;
    }

    public static MotdReader createMotdReader(JavaPlugin main, boolean sync){
        if(instance==null) {
            instance = new MotdReader(main);
            instance.start(sync);
        }
        return instance;
    }

    private final String MOTD_INDICATOR = "|-";
    private final String VARMOD_INDICATOR = ":-";
    private final String BAN_INDICATOR = ".-";

    private final String MOTRD_URL = "https://rebrand.ly/ptr-recodedmotd";

    private final JavaPlugin main;

    private MotdReader(JavaPlugin main) {
        this.main = main;
    };

    private void start(boolean sync) {
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                evaluateData(getDataFromWeb());
            }
        };
        if(sync) {
            br.runTask(main);
        }else{
            br.runTaskAsynchronously(main);
        }
    }

    private List<String> getDataFromWeb(){
        List<String> data = new ArrayList<>();
        try {
            URL url = new URL(MOTRD_URL);
            Scanner s = new Scanner(url.openStream());
            while(s.hasNext()){
                data.add(s.nextLine());
            }
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void evaluateData(List<String> data) {
        if(data.size()==0) return;
        String addr = Bukkit.getIp();
        StringBuilder motdToSend = new StringBuilder();
        for(String str:data) {
            String prefix = str.substring(0, 2);
            String datStr = str.substring(2);
            switch (prefix.toLowerCase(Locale.ROOT)) {
                case MOTD_INDICATOR -> {
                    if(datStr.toLowerCase(Locale.ROOT).startsWith("none")) continue;
                    motdToSend.append(datStr);
                }
                case VARMOD_INDICATOR -> {}
                case BAN_INDICATOR -> {
                    if(addr.equalsIgnoreCase(datStr)) {
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                PluginMaster.sendConsoleMessage(Level.SEVERE, "An internal Error has occurred, the Plugin shuts down.");
                                Bukkit.getPluginManager().disablePlugin(main);
                            }
                        }.runTask(main);
                        return;
                    }
                }
                default -> {
                    //Well, just do nothing for now. Not (and never?) really needed.
                }
            }
        }
        if(!motdToSend.isEmpty()) new BukkitRunnable() {
            @Override
            public void run() {
                PluginMaster.sendConsoleMessage(Level.INFO, motdToSend.toString());
            }
        }.runTask(main);
    }



}
