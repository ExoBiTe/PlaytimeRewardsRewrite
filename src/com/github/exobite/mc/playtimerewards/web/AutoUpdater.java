package com.github.exobite.mc.playtimerewards.web;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class AutoUpdater {

    private static AutoUpdater instance;

    public static AutoUpdater getInstance(){
        return instance;
    }

    public static AutoUpdater createAutoUpdater(JavaPlugin main, boolean sync) {
        if(instance==null){
            instance = new AutoUpdater(main);
            instance.start(sync);
        }
        return instance;
    }

    private final String MY_USER_AGENT = "ExobitePlugin";
    private final int RESOURCE_ID = 100231;
    private final String GET_LATEST_VERSION = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/versions/latest";

    private final JavaPlugin main;

    private String latestVersion = null;
    private boolean updateAvailable = false;

    private AutoUpdater(JavaPlugin main) {
        this.main = main;
    }

    private void start(boolean sync) {
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                boolean response = checkForNewerVersion(true, true, true);
                if(response) {
                    updateAvailable = true;
                    //Send the Message on the Main thread for a nicer-looking console prefix.
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PluginMaster.sendConsoleMessage(Level.INFO, "A new Version (v"+latestVersion+") is available!");
                        }
                    }.runTask(main);
                }
            }
        };
        if(sync) {
            br.runTask(main);
        }else{
            br.runTaskAsynchronously(main);
        }
    }

    private boolean checkForNewerVersion(boolean checkAtMajor, boolean checkAtMinor, boolean checkAtPatch) {
        String currentVersion = main.getDescription().getVersion();
        try {
            URL url = new URL(GET_LATEST_VERSION);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't check for a newer Update, HTTP Error code: "+responseCode);
                return false;
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            JsonElement e = new JsonParser().parse(isr);

            latestVersion = unpackVersionFromJson(e);
            if(latestVersion==null) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't find the newest Latest Version in the HTTP Response.");
                return false;
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            latestVersion = "0.0.0";
        }

        return latestIsNewer(latestVersion, currentVersion, checkAtMajor, checkAtMinor, checkAtPatch);
    }

    private String unpackVersionFromJson(JsonElement e){
        if(!(e instanceof JsonObject jo)) return null;
        return jo.get("name").getAsString();
    }

    private boolean latestIsNewer(String versionLatest, String versionLocal, boolean checkAtMajor, boolean checkAtMinor, boolean checkAtPatch) {
        String[] splitLatest = versionLatest.split("\\.");
        String[] splitLocal = versionLocal.split("\\.");
        if(splitLatest.length<2 || splitLocal.length<2) {
            return false;
        }
        int[] numbersLatest = new int[splitLatest.length];
        int[] numbersLocal = new int[splitLocal.length];
        for(int i=0;i<splitLatest.length;i++) {
            numbersLatest[i] = Integer.parseInt(splitLatest[i]);
        }
        for(int i=0;i<splitLocal.length;i++) {
            numbersLocal[i] = Integer.parseInt(splitLocal[i]);
        }
        return (checkAtMajor && numbersLatest[0] > numbersLocal[0]) ||
                (checkAtMinor && numbersLatest[0] >= numbersLocal[0] && numbersLatest[1] > numbersLocal[1]) ||
                (checkAtPatch && (numbersLatest.length > numbersLocal.length ||
                        (numbersLatest.length >= 3 && numbersLatest[2] > numbersLocal[2])));

    }

    public boolean isUpdateAvailable(){
        return updateAvailable;
    }

    public String getLatestVersion(){
        return latestVersion;
    }


}
